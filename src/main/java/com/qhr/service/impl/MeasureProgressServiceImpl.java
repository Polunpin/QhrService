package com.qhr.service.impl;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.enums.MeasureProgressStage;
import com.qhr.model.FinancingIntention;
import com.qhr.model.StatusLog;
import com.qhr.service.FinancingIntentionService;
import com.qhr.service.MeasureProgressService;
import com.qhr.service.StatusLogService;
import com.qhr.vo.MeasureProgressVO;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于状态日志表的测额进度实现。
 * 通过读取最新阶段日志构造前端可直接消费的进度快照。
 */
@ApplicationScoped
public class MeasureProgressServiceImpl implements MeasureProgressService {

    private static final int STAGE_TOTAL = 4;

    private final StatusLogService statusLogService;
    private final FinancingIntentionService financingIntentionService;
    private final ScheduledExecutorService scheduler;

    public MeasureProgressServiceImpl(StatusLogService statusLogService,
                                      FinancingIntentionService financingIntentionService) {
        this.statusLogService = statusLogService;
        this.financingIntentionService = financingIntentionService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * 初始化任务进度，写入“预审通过”阶段。
     */
    @Override
    public void markSubmitted(Long intentionId) {
        markStage(intentionId, MeasureProgressStage.PRECHECK_PASSED, null);
    }

    /**
     * 为指定任务追加一条阶段日志。
     * 如果阶段未发生变化，则忽略重复写入。
     */
    @Override
    public void markStage(Long intentionId, MeasureProgressStage stage, String remark) {
        ApiAssert.notNull(intentionId, ApiCode.BAD_REQUEST, "intentionId不能为空");
        ApiAssert.notNull(stage, ApiCode.BAD_REQUEST, "进度阶段不能为空");

        MeasureProgressStage latestStage = getLatestStage(intentionId);
        if (latestStage == stage) {
            return;
        }

        StatusLog log = new StatusLog();
        log.setOrderId(intentionId);
        log.setOperatorType("system");
        log.setPreStage(latestStage == null ? null : latestStage.code());
        log.setPostStage(stage.code());
        log.setRemark(hasText(remark) ? remark : stage.description());
        statusLogService.create(log);
    }

    /**
     * 将任务标记为失败终态。
     */
    @Override
    public void markFailed(Long intentionId, String remark) {
        MeasureProgressStage latestStage = getLatestStage(intentionId);
        if (latestStage == MeasureProgressStage.FAILED) {
            return;
        }

        StatusLog log = new StatusLog();
        log.setOrderId(intentionId);
        log.setOperatorType("system");
        log.setPreStage(latestStage == null ? null : latestStage.code());
        log.setPostStage(MeasureProgressStage.FAILED.code());
        log.setRemark(hasText(remark) ? remark : MeasureProgressStage.FAILED.description());
        statusLogService.create(log);
    }

    /**
     * 按用户权限读取任务进度快照。
     */
    @Override
    public MeasureProgressVO getProgress(Long intentionId, String openid) {
        FinancingIntention intention = financingIntentionService.getById(intentionId);
        ApiAssert.notNull(intention, ApiCode.NOT_FOUND, "融资需求不存在");
        ApiAssert.isTrue(hasText(openid) && openid.equals(intention.getUserOpenId()),
                ApiCode.NOT_FOUND, "融资需求不存在");
        return buildSnapshot(intentionId).payload();
    }

    /**
     * 定时轮询最新日志，并仅在进度变化时向 SSE 客户端推送新快照。
     */
    @Override
    public Multi<MeasureProgressVO> stream(Long intentionId, String openid) {
        getProgress(intentionId, openid);

        return Multi.createFrom().emitter(emitter -> {
            MeasureProgressSnapshot initial = buildSnapshot(intentionId);
            emitter.emit(initial.payload());
            if (initial.terminal()) {
                emitter.complete();
                return;
            }

            AtomicLong lastVersion = new AtomicLong(initial.version());
            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
                try {
                    MeasureProgressSnapshot snapshot = buildSnapshot(intentionId);
                    if (snapshot.version() > lastVersion.get()) {
                        lastVersion.set(snapshot.version());
                        emitter.emit(snapshot.payload());
                    }
                    if (snapshot.terminal()) {
                        emitter.complete();
                    }
                } catch (Throwable throwable) {
                    emitter.fail(throwable);
                }
            }, 1, 1, TimeUnit.SECONDS);

            emitter.onTermination(() -> future.cancel(true));
        });
    }

    /**
     * 从状态日志中聚合出当前任务的轻量进度视图。
     */
    private MeasureProgressSnapshot buildSnapshot(Long intentionId) {
        List<StatusLog> logs = statusLogService.listByOrderId(intentionId, 0, 20);
        StatusLog latestLog = logs.isEmpty() ? null : logs.getFirst();
        MeasureProgressStage latestStage = latestLog == null
                ? MeasureProgressStage.PRECHECK_PASSED
                : defaultStage(MeasureProgressStage.fromCode(latestLog.getPostStage()));
        MeasureProgressStage currentStage = resolveCurrentStage(latestStage, latestLog);
        MeasureProgressStage failedAtStage = resolveFailedAtStage(latestLog);

        return new MeasureProgressSnapshot(
                new MeasureProgressVO(
                        currentStage.stageKey(),
                        currentStage.step(),
                        STAGE_TOTAL,
                        currentStage.title(),
                        resolveMessage(latestLog, latestStage, currentStage),
                        resolveOverallStatus(latestStage),
                        resolveProgressPercent(latestStage, failedAtStage)
                ),
                latestLog == null ? 0L : latestLog.getId(),
                isTerminal(latestStage)
        );
    }

    /**
     * 将内部阶段映射为前端当前展示的阶段。
     * 预审通过后默认进入“生成企业画像”，完成态则停留在最后一个展示阶段。
     */
    private MeasureProgressStage resolveCurrentStage(MeasureProgressStage latestStage, StatusLog latestLog) {
        if (latestStage == MeasureProgressStage.PRECHECK_PASSED) {
            return MeasureProgressStage.PROFILE_BUILDING;
        }
        if (latestStage == MeasureProgressStage.COMPLETED) {
            return MeasureProgressStage.RECOMMENDATION_GENERATING;
        }
        if (latestStage == MeasureProgressStage.FAILED) {
            return resolveFailedAtStage(latestLog);
        }
        return latestStage;
    }

    /**
     * 将当前阶段换算为前端使用的进度百分比。
     */
    private int resolveProgressPercent(MeasureProgressStage latestStage,
                                       MeasureProgressStage failedAtStage) {
        if (latestStage == MeasureProgressStage.FAILED) {
            return progressPercentOf(failedAtStage);
        }
        return progressPercentOf(latestStage);
    }

    /**
     * 定义各阶段的进度百分比映射。
     */
    private int progressPercentOf(MeasureProgressStage stage) {
        return switch (stage) {
            case PRECHECK_PASSED -> 25;
            case PROFILE_BUILDING -> 52;
            case PRODUCT_MATCHING -> 78;
            case RECOMMENDATION_GENERATING, FAILED -> 90;
            case COMPLETED -> 100;
        };
    }

    /**
     * 推导任务总体状态。
     */
    private String resolveOverallStatus(MeasureProgressStage latestStage) {
        if (latestStage == MeasureProgressStage.COMPLETED) {
            return "success";
        }
        if (latestStage == MeasureProgressStage.FAILED) {
            return "failed";
        }
        return "running";
    }

    /**
     * 生成页面主描述，优先使用日志中的说明文本。
     */
    private String resolveMessage(StatusLog latestLog,
                                  MeasureProgressStage latestStage,
                                  MeasureProgressStage currentStage) {
        if (hasText(latestLog == null ? null : latestLog.getRemark())) {
            return latestLog.getRemark();
        }
        if (latestStage == MeasureProgressStage.PRECHECK_PASSED) {
            return "系统已完成预审，正在生成企业画像";
        }
        return currentStage.description();
    }

    /**
     * 失败时推导停留在哪个展示阶段，便于前端高亮失败节点。
     */
    private MeasureProgressStage resolveFailedAtStage(StatusLog latestLog) {
        if (latestLog == null || !MeasureProgressStage.FAILED.code().equals(latestLog.getPostStage())) {
            return MeasureProgressStage.RECOMMENDATION_GENERATING;
        }

        MeasureProgressStage stage = MeasureProgressStage.fromCode(latestLog.getPreStage());
        if (stage == null || !stage.isDisplayStage()) {
            return MeasureProgressStage.RECOMMENDATION_GENERATING;
        }
        return stage;
    }

    /**
     * 查询任务最近一次写入的阶段。
     */
    private MeasureProgressStage getLatestStage(Long intentionId) {
        List<StatusLog> logs = statusLogService.listByOrderId(intentionId, 0, 1);
        if (logs.isEmpty()) {
            return null;
        }
        return MeasureProgressStage.fromCode(logs.getFirst().getPostStage());
    }

    /**
     * 对未知阶段做兜底处理，保证前端至少能展示初始状态。
     */
    private MeasureProgressStage defaultStage(MeasureProgressStage stage) {
        return stage == null ? MeasureProgressStage.PRECHECK_PASSED : stage;
    }

    /**
     * 简单的非空白文本判断。
     */
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isTerminal(MeasureProgressStage latestStage) {
        return latestStage == MeasureProgressStage.COMPLETED
                || latestStage == MeasureProgressStage.FAILED;
    }

    @PreDestroy
    void shutdown() {
        scheduler.shutdown();
    }

    private record MeasureProgressSnapshot(MeasureProgressVO payload, long version, boolean terminal) {
    }
}
