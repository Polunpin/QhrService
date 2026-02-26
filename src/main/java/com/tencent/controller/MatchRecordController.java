package com.tencent.controller;

import com.tencent.config.ApiResponse;
import com.tencent.dto.UpdateStringStatusRequest;
import com.tencent.model.MatchRecord;
import com.tencent.service.MatchRecordService;
import com.tencent.vo.MatchRecords;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.tencent.config.ApiAssert;
import com.tencent.config.ApiCode;
import com.tencent.config.PageBounds;
import com.tencent.config.PageResult;

@RestController
@RequestMapping("/api/matching")
public class MatchRecordController {

  @Resource
  private MatchRecordService matchRecordService;

  /** 查询匹配记录详情 */
  @GetMapping("/{id}")
  public ApiResponse getById(@PathVariable Long id) {
    MatchRecord record = matchRecordService.getById(id);
    ApiAssert.notNull(record, ApiCode.NOT_FOUND, "匹配记录不存在");
    return ApiResponse.ok(record);
  }

  /** 分页查询匹配记录列表 */
  @GetMapping("/list")
  public ApiResponse list(@RequestParam(required = false) Long enterpriseId,
                          @RequestParam(required = false) Long intentionId,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) Integer page,
                          @RequestParam(required = false) Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<MatchRecords> records = matchRecordService.list(enterpriseId, intentionId, status,
        bounds.offset(), bounds.size());
    long total = matchRecordService.count(enterpriseId, intentionId, status);
    return ApiResponse.ok(PageResult.of(records, total, bounds.page(), bounds.size()));
  }

  /** 创建匹配记录 */
  @PostMapping
  public ApiResponse create(@RequestBody MatchRecord record) {
    Long id = matchRecordService.create(record);
    return ApiResponse.ok(id);
  }

  /** 更新匹配记录 */
  @PutMapping("/{id}")
  public ApiResponse update(@PathVariable Long id, @RequestBody MatchRecord record) {
    ApiAssert.isTrue(matchRecordService.update(record.withId(id)), ApiCode.NOT_FOUND, "匹配记录不存在");
    return ApiResponse.ok(true);
  }

  /** 删除匹配记录 */
  @DeleteMapping("/{id}")
  public ApiResponse delete(@PathVariable Long id) {
    ApiAssert.isTrue(matchRecordService.delete(id), ApiCode.NOT_FOUND, "匹配记录不存在");
    return ApiResponse.ok(true);
  }

  /** 更新匹配记录状态 */
  @PostMapping("/{id}/status")
  public ApiResponse updateStatus(@PathVariable Long id,
                                  @RequestBody UpdateStringStatusRequest request) {
    ApiAssert.isTrue(matchRecordService.updateStatus(id, request.status()),
        ApiCode.NOT_FOUND, "匹配记录不存在");
    return ApiResponse.ok(true);
  }
}
