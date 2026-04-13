package com.qhr.service.impl;

import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.service.CreditReportParseService;
import com.qhr.service.WeixinCloudFileService;
import com.qhr.vo.credit.EnterpriseCreditReportRaw;
import com.qhr.vo.credit.PersonalCreditReportRaw;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 个人征信 PDF 解析器。
 * 当前实现面向官方文本型 PDF，通过章节切分和正则提取结构化字段。
 */
@ApplicationScoped
public class CreditReportParseServiceImpl implements CreditReportParseService {

    private static final Pattern REPORT_HEADER_PATTERN = Pattern.compile(
            "报告编号[:：]\\s*(\\S+)\\s+报告时间[:：]\\s*(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})");
    private static final Pattern PERSON_HEADER_PATTERN = Pattern.compile(
            "姓名[:：]\\s*(\\S+)\\s+证件类型[:：]\\s*(\\S+)\\s+证件号码[:：]\\s*(\\S+)\\s*(已婚|未婚|离异)?");
    private static final Pattern CREDIT_CARD_SECTION_PATTERN = Pattern.compile(
            "发生过逾期的贷记卡账户明细如下：(?<content>.*?)贷款\\s*从未发生过逾期的账户明细如下：",
            Pattern.DOTALL);
    private static final Pattern LOAN_SECTION_PATTERN = Pattern.compile(
            "贷款\\s*从未发生过逾期的账户明细如下：(?<content>.*?)相关还款责任信息",
            Pattern.DOTALL);
    private static final Pattern LIABILITY_SECTION_PATTERN = Pattern.compile(
            "相关还款责任信息(?<content>.*?)非信贷交易记录",
            Pattern.DOTALL);
    private static final Pattern INSTITUTION_QUERY_SECTION_PATTERN = Pattern.compile(
            "机构查询记录明细(?<content>.*?)个人查询记录明细",
            Pattern.DOTALL);
    private static final Pattern SELF_QUERY_SECTION_PATTERN = Pattern.compile(
            "个人查询记录明细(?<content>.*?)说\\s*明",
            Pattern.DOTALL);
    private static final Pattern CREDIT_CARD_BLOCK_PATTERN = Pattern.compile(
            "(\\d{4}年\\d{2}月\\d{2}日.+?发放的(?:贷记卡|准贷记卡).*?。(?:\\s*最近5年内有\\d+个月处于逾期状态，(?:没有)?发生过90天以上逾期。)?)"
                    + "(?=\\s*\\d{4}年\\d{2}月\\d{2}日.+?发放的(?:贷记卡|准贷记卡)|\\s*$)");
    private static final Pattern LOAN_BLOCK_PATTERN = Pattern.compile(
            "(\\d{4}年\\d{2}月\\d{2}日.+?(?:为.+?授信|发放的.+?贷款).*?。)"
                    + "(?=\\s*\\d{4}年\\d{2}月\\d{2}日.+?(?:为.+?授信|发放的.+?贷款)|\\s*$)");
    private static final Pattern LIABILITY_BLOCK_PATTERN = Pattern.compile(
            "(\\d{4}年\\d{2}月\\d{2}日，为.+?贷款余\\s*额[\\d,]+（人民币元）。)"
                    + "(?=\\s*\\d{4}年\\d{2}月\\d{2}日，为|\\s*$)");
    private static final Pattern CARD_ISSUE_PATTERN = Pattern.compile(
            "^(\\d{4}年\\d{2}月\\d{2}日)(.+?)发放的(贷记卡|准贷记卡)（([^）]+)）(.*)$");
    private static final Pattern MONTH_PATTERN = Pattern.compile("(\\d{4}年\\d{2}月)");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}年\\d{2}月\\d{2}日)");
    private static final Pattern CARD_TAIL_PATTERN = Pattern.compile("卡片尾号[:：]\\s*(\\d+)");
    private static final Pattern CREDIT_LIMIT_PATTERN = Pattern.compile("信用额度\\s*([\\d,]+)");
    private static final Pattern BALANCE_PATTERN = Pattern.compile("余额为?\\s*([\\d,]+)");
    private static final Pattern OVERDUE_MONTHS_PATTERN = Pattern.compile("最近5年内有(\\d+)个月处于逾期状态");
    private static final Pattern REVOLVING_LOAN_PATTERN = Pattern.compile(
            "^(\\d{4}年\\d{2}月\\d{2}日)(.+?)为(.+?)授信，额度有效期至(\\d{4}年\\d{2}月\\d{2}日)，"
                    + "(可循环使用|不可循环使用)。截至\\s*(\\d{4}年\\d{2}月)，信用额度([\\d,]+)元（([^）]+)），"
                    + "余额为([\\d,]+)，当前([^。]+)。?$");
    private static final Pattern ISSUED_LOAN_SETTLED_PATTERN = Pattern.compile(
            "^(\\d{4}年\\d{2}月\\d{2}日)(.+?)发放的([\\d,]+)元（([^）]+)）(.+?贷款)，(\\d{4}年\\d{2}月)已结清。?$");
    private static final Pattern LIABILITY_PATTERN = Pattern.compile(
            "^(\\d{4}年\\d{2}月\\d{2}日)，为(.+?)（证件类型：([^，]+)，证件号码：([^）]+)）"
                    + "在(.+?)办理的贷款承担相关还款责任，责任人类型为([^，]+)，相关还款责任金额([^（，。]+"
                    + ")(?:（[^）]*）)?。截至(\\d{4}年\\d{2}月\\d{2}日)，贷款余\\s*额([\\d,]+|--)（人民币元）。?$");
    private static final Pattern QUERY_RECORD_PATTERN = Pattern.compile("^(\\d+)\\s+(\\d{4}年\\d{2}月\\d{2}日)\\s+(.+)$");
    private static final Pattern ENTERPRISE_REPORT_NO_PATTERN = Pattern.compile("NO\\.(\\S+)");
    private static final Pattern ENTERPRISE_COMPANY_NAME_PATTERN = Pattern.compile("企业名称[:：]?\\s*(\\S+)");
    private static final Pattern ENTERPRISE_CENT_CODE_PATTERN = Pattern.compile("中征码[:：]?\\s*(\\S+)");
    private static final Pattern ENTERPRISE_UNIFIED_CODE_PATTERN = Pattern.compile("统一社会信用代码[:：]?\\s*(\\S+)");
    private static final Pattern ENTERPRISE_QUERY_INSTITUTION_PATTERN = Pattern.compile("查询机构[:：]?\\s*(.+?)\\s+报告时间");
    private static final Pattern ENTERPRISE_REPORT_TIME_PATTERN = Pattern.compile("报告时间[:：]?\\s*(\\S+)");
    private static final Pattern ENTERPRISE_SUMMARY_YEARS_PATTERN = Pattern.compile(
            "首次有信贷交易的年份 发生信贷交易的机构数 当前有未结清信贷 交易的机构数 首次有相关还款 责任的年份 (\\S+) (\\S+) (\\S+) (\\S+)");
    private static final Pattern ENTERPRISE_SUMMARY_BALANCES_PATTERN = Pattern.compile(
            "借贷交易 担保交易 余额 (\\S+) 余额 (\\S+) 其中：被追偿余额 (\\S+) 其中：关注类余额 (\\S+) 关注类余额 (\\S+) 不良类余额 (\\S+) 不良类余额 (\\S+)");
    private static final Pattern ENTERPRISE_PUBLIC_COUNTS_PATTERN = Pattern.compile(
            "非信贷交易账户数 欠税记录条数 民事判决记录条数 强制执行记录条数 行政处罚记录条数 (\\S+) (\\S+) (\\S+) (\\S+) (\\S+)");
    private static final Pattern ENTERPRISE_SHORT_TERM_SUMMARY_PATTERN = Pattern.compile(
            "短期借款 (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+)");
    private static final Pattern ENTERPRISE_GUARANTEE_SUMMARY_PATTERN = Pattern.compile(
            "其他担保交易 (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+)");
    private static final Pattern ENTERPRISE_CREDIT_SUMMARY_PATTERN = Pattern.compile(
            "非循环信用额度 循环信用额度 总额 已用额度 剩余可用额度 总额 已用额度 剩余可用额度 (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+)");
    private static final Pattern ENTERPRISE_RELATED_REPAYMENT_SUMMARY_PATTERN = Pattern.compile(
            "保证人/反担保人 (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+) (\\S+)");
    private static final String ENTERPRISE_BASIC_FIELD_PATTERN_TEMPLATE = "%s\\s+(.+?)\\s+信息来源机构";
    private static final Pattern ENTERPRISE_REGISTERED_CAPITAL_PATTERN = Pattern.compile("注册资本折人民币合计\\s*(\\S+)");
    private static final Pattern ENTERPRISE_CURRENT_LOAN_LINE_PATTERN = Pattern.compile(
            "(.+?(?:贷款|融资|保理|票据贴现))\\s+(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{4}-\\d{2}-\\d{2})\\s+(人民币元|美元|欧元|港币)\\s+([\\d.]+)\\s+(新增|续作|--)");
    private static final Pattern ENTERPRISE_CURRENT_LOAN_DETAIL_PATTERN = Pattern.compile(
            "(.+?)\\s+([\\d.]+)\\s+(正常|关注|次级|可疑|损失|违约|未分类)\\s+([\\d.]+)\\s+([\\d.]+)\\s+([\\dN]+)\\s+(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern ENTERPRISE_CURRENT_LOAN_TAIL_PATTERN = Pattern.compile(
            "([\\d.]+)\\s+(正常还款|逾期还款|部分还款|--)");
    private static final Pattern ENTERPRISE_TAX_LEVEL_PATTERN = Pattern.compile("纳税信用([ABMCD])级纳税人");
    private static final List<String> QUERY_REASONS = List.of(
            "本人查询（商业银行网上银行）",
            "法人代表、负责人、高管等资信审查",
            "担保资格审查",
            "信用卡审批",
            "贷款审批",
            "贷后管理"
    );

    private final WeixinCloudFileService weixinCloudFileService;

    public CreditReportParseServiceImpl(WeixinCloudFileService weixinCloudFileService) {
        this.weixinCloudFileService = weixinCloudFileService;
    }

    /**
     * 云文件解析主入口：下载 PDF、解析内容，再按策略尝试删除远端文件。
     */
    @Override
    public PersonalCreditReportRaw parsePersonalCloudFile(String fileId) {

        byte[] pdfBytes = weixinCloudFileService.download(fileId);
        PersonalCreditReportRaw report = parsePersonalPdf(pdfBytes);

        try {
            weixinCloudFileService.delete(fileId);
        } catch (ApiException exception) {
            report.getParseWarnings().add("云文件删除失败: " + exception.getMessage());
        }
        return report;
    }

    /**
     * 云文件解析主入口：下载企业征信 PDF、解析内容，再按策略尝试删除远端文件。
     */
    @Override
    public EnterpriseCreditReportRaw parseEnterpriseCloudFile(String fileId) {
        byte[] pdfBytes = weixinCloudFileService.download(fileId);
        EnterpriseCreditReportRaw report = parseEnterprisePdf(pdfBytes);
        try {
            weixinCloudFileService.delete(fileId);
        } catch (ApiException exception) {
            report.getParseWarnings().add("云文件删除失败: " + exception.getMessage());
        }
        return report;
    }

    /**
     * 直接解析 PDF 字节流，只接受个人征信文本型 PDF。
     */
    @Override
    public PersonalCreditReportRaw parsePersonalPdf(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new ApiException(ApiCode.BAD_REQUEST, "PDF内容不能为空");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String rawText = stripper.getText(document);
            if (rawText == null || rawText.isBlank()) {
                throw new ApiException(ApiCode.BAD_REQUEST, "PDF文本提取失败，可能是扫描件或加密文件");
            }
            if (!rawText.replaceAll("\\s+", "").contains("个人信用报告")) {
                throw new ApiException(ApiCode.BAD_REQUEST, "当前仅支持个人信用报告PDF解析");
            }
            PersonalCreditReportRaw report = new PersonalCreditReportRaw();
            report.setPageCount(document.getNumberOfPages());
            populateHeaders(rawText, report);
            populateFlags(rawText, report);
            populateCreditCards(rawText, report);
            populateLoans(rawText, report);
            populateRelatedLiabilities(rawText, report);
            populateInstitutionQueries(rawText, report);
            populateSelfQueries(rawText, report);
            populateSummary(report);
            return report;
        } catch (IOException exception) {
            throw new ApiException(ApiCode.BAD_REQUEST, "PDF解析失败: " + exception.getMessage());
        }
    }

    /**
     * 直接解析 PDF 字节流，只接受企业信用报告文本型 PDF。
     */
    @Override
    public EnterpriseCreditReportRaw parseEnterprisePdf(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new ApiException(ApiCode.BAD_REQUEST, "PDF内容不能为空");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String rawText = stripper.getText(document);
            if (rawText == null || rawText.isBlank()) {
                throw new ApiException(ApiCode.BAD_REQUEST, "PDF文本提取失败，可能是扫描件或加密文件");
            }
            if (!rawText.replaceAll("\\s+", "").contains("企业信用报告")) {
                throw new ApiException(ApiCode.BAD_REQUEST, "当前仅支持企业信用报告PDF解析");
            }

            EnterpriseCreditReportRaw report = new EnterpriseCreditReportRaw();
            report.setPageCount(document.getNumberOfPages());
            List<String> pageTexts = extractPageTexts(document);
            populateEnterpriseHeaders(rawText, report);
            populateEnterpriseSummary(pageTexts, report);
            populateEnterpriseBasicProfile(pageTexts, report);
            populateEnterpriseUnsettledLoans(pageTexts, report);
            populateEnterpriseTaxLevels(pageTexts, report);
            return report;
        } catch (IOException exception) {
            throw new ApiException(ApiCode.BAD_REQUEST, "PDF解析失败: " + exception.getMessage());
        }
    }

    /**
     * 提取报告头中的报告编号、报告时间和个人基础身份信息。
     */
    private void populateHeaders(String text, PersonalCreditReportRaw report) {
        Matcher headerMatcher = REPORT_HEADER_PATTERN.matcher(normalizeSpaces(text));
        if (headerMatcher.find()) {
            report.setReportNumber(headerMatcher.group(1));
            report.setReportTime(headerMatcher.group(2));
        } else {
            report.getParseWarnings().add("未识别报告编号与报告时间");
        }

        Matcher personMatcher = PERSON_HEADER_PATTERN.matcher(normalizeSpaces(text));
        if (personMatcher.find()) {
            report.getPerson().setName(personMatcher.group(1));
            report.getPerson().setIdType(personMatcher.group(2));
            report.getPerson().setIdNo(personMatcher.group(3));
            report.getPerson().setMaritalStatus(personMatcher.group(4));
        } else {
            report.getParseWarnings().add("未识别个人基础信息");
        }
    }

    /**
     * 标记报告中是否存在非信贷记录和公共记录。
     */
    private void populateFlags(String text, PersonalCreditReportRaw report) {
        report.setHasNonCreditRecords(!text.contains("系统中没有您最近5年内的非信贷交易记录"));
        report.setHasPublicRecords(!text.contains("系统中没有您最近5年内的公共信息记录"));
    }

    /**
     * 解析信用卡章节，兼容逾期卡、销户卡和未激活卡三类句式。
     */
    private void populateCreditCards(String text, PersonalCreditReportRaw report) {
        String section = extractSection(text, CREDIT_CARD_SECTION_PATTERN, "信用卡明细");
        if (section == null) {
            return;
        }
        String normalized = cleanupNarrativeSection(section)
                .replace("从未逾期过的贷记卡及透支未超过60天的准贷记卡账户明细如下：", " ");
        for (String block : extractBlocks(normalized, CREDIT_CARD_BLOCK_PATTERN)) {
            PersonalCreditReportRaw.CreditCardAccount account = parseCreditCardBlock(block);
            if (account == null) {
                report.getParseWarnings().add("未识别信用卡记录: " + abbreviate(block));
                continue;
            }
            report.getCreditCards().add(account);
        }
    }

    /**
     * 解析贷款章节，兼容循环授信和已结清发放贷款两类描述。
     */
    private void populateLoans(String text, PersonalCreditReportRaw report) {
        String section = extractSection(text, LOAN_SECTION_PATTERN, "贷款明细");
        if (section == null) {
            return;
        }
        String normalized = cleanupNarrativeSection(section)
                .replaceAll("信贷记录\\s*这部分包含您的信用卡、贷款和其他信贷记录。.*?相关还款责任账户数\\s*--\\s*\\d+", " ");
        for (String block : extractBlocks(normalized, LOAN_BLOCK_PATTERN)) {
            PersonalCreditReportRaw.LoanAccount account = parseLoanBlock(block);
            if (account == null) {
                report.getParseWarnings().add("未识别贷款记录: " + abbreviate(block));
                continue;
            }
            report.getLoans().add(account);
        }
    }

    /**
     * 解析“相关还款责任信息”，用于识别保证人和共同借款人负债。
     */
    private void populateRelatedLiabilities(String text, PersonalCreditReportRaw report) {
        String section = extractSection(text, LIABILITY_SECTION_PATTERN, "相关还款责任");
        if (section == null) {
            return;
        }
        String normalized = cleanupNarrativeSection(section);
        for (String block : extractBlocks(normalized, LIABILITY_BLOCK_PATTERN)) {
            Matcher matcher = LIABILITY_PATTERN.matcher(block);
            if (!matcher.matches()) {
                report.getParseWarnings().add("未识别相关还款责任记录: " + abbreviate(block));
                continue;
            }
            PersonalCreditReportRaw.RelatedRepaymentLiability item = new PersonalCreditReportRaw.RelatedRepaymentLiability();
            item.setIssueDate(matcher.group(1));
            item.setCompanyName(normalizeWrappedChinese(matcher.group(2)));
            item.setCompanyIdType(matcher.group(3));
            item.setCompanyIdNo(matcher.group(4));
            item.setInstitution(normalizeWrappedChinese(matcher.group(5)));
            item.setLiabilityType(matcher.group(6));
            item.setLiabilityAmount(parseAmount(matcher.group(7)));
            item.setAsOfDate(matcher.group(8));
            item.setLoanBalance(parseAmount(matcher.group(9)));
            report.getRelatedLiabilities().add(item);
        }
    }

    /**
     * 解析机构查询记录明细。
     */
    private void populateInstitutionQueries(String text, PersonalCreditReportRaw report) {
        String section = extractSection(text, INSTITUTION_QUERY_SECTION_PATTERN, "机构查询记录");
        if (section == null) {
            return;
        }
        report.setInstitutionQueries(parseQueries(section, report.getParseWarnings(), "机构查询"));
    }

    /**
     * 解析本人查询记录明细。
     */
    private void populateSelfQueries(String text, PersonalCreditReportRaw report) {
        String section = extractSection(text, SELF_QUERY_SECTION_PATTERN, "个人查询记录");
        if (section == null) {
            return;
        }
        report.setSelfQueries(parseQueries(section, report.getParseWarnings(), "个人查询"));
    }

    /**
     * 根据已解析的明细回填摘要统计，方便后续画像层直接复用。
     */
    private void populateSummary(PersonalCreditReportRaw report) {
        PersonalCreditReportRaw.Summary summary = report.getSummary();
        summary.setCreditCardAccountCount(report.getCreditCards().size());
        summary.setActiveCreditCardCount((int) report.getCreditCards().stream()
                .filter(account -> !"CLOSED".equals(account.getAccountStatus()))
                .count());
        summary.setOverdueCreditCardAccountCount((int) report.getCreditCards().stream()
                .filter(account -> Boolean.TRUE.equals(account.getCurrentOverdue())
                        || (account.getOverdueMonthsInLast5Years() != null && account.getOverdueMonthsInLast5Years() > 0))
                .count());
        summary.setOverdue90PlusCreditCardAccountCount((int) report.getCreditCards().stream()
                .filter(account -> Boolean.TRUE.equals(account.getHasOverdue90Plus()))
                .count());
        summary.setLoanAccountCount(report.getLoans().size());
        summary.setActiveLoanCount((int) report.getLoans().stream()
                .filter(account -> !"SETTLED".equals(account.getCurrentStatus()))
                .count());
        summary.setRelatedRepaymentLiabilityCount(report.getRelatedLiabilities().size());
        summary.setInstitutionQueryCount(report.getInstitutionQueries().size());
        summary.setSelfQueryCount(report.getSelfQueries().size());
    }

    /**
     * 按页抽取 PDF 文本，便于企业征信这种强版式报告按页定位章节。
     */
    private List<String> extractPageTexts(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        List<String> pageTexts = new ArrayList<>();
        for (int page = 1; page <= document.getNumberOfPages(); page++) {
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            pageTexts.add(stripper.getText(document));
        }
        return pageTexts;
    }

    /**
     * 提取企业征信报告头：报告编号、报告时间、企业名称、中征码和统一社会信用代码。
     */
    private void populateEnterpriseHeaders(String text, EnterpriseCreditReportRaw report) {
        String normalized = normalizeSpaces(text);
        report.setReportNumber(extractFirst(normalized, ENTERPRISE_REPORT_NO_PATTERN));
        report.setReportTime(extractFirst(normalized, ENTERPRISE_REPORT_TIME_PATTERN));
        report.getEnterprise().setCompanyName(extractFirst(normalized, ENTERPRISE_COMPANY_NAME_PATTERN));
        report.getEnterprise().setCentCode(extractFirst(normalized, ENTERPRISE_CENT_CODE_PATTERN));
        report.getEnterprise().setUnifiedSocialCreditCode(extractFirst(normalized, ENTERPRISE_UNIFIED_CODE_PATTERN));
        report.getEnterprise().setQueryInstitution(extractFirst(normalized, ENTERPRISE_QUERY_INSTITUTION_PATTERN));
    }

    /**
     * 提取企业征信概要页中的机构数、余额、公共记录条数和授信额度摘要。
     */
    private void populateEnterpriseSummary(List<String> pageTexts, EnterpriseCreditReportRaw report) {
        if (pageTexts.size() < 3) {
            report.getParseWarnings().add("企业征信报告页数不足，无法提取信息概要");
            return;
        }
        List<String> lines = cleanEnterpriseLines(pageTexts.get(2));
        String normalized = cleanupEnterprisePage(pageTexts.get(2))
                .replace("其他担保交 易", "其他担保交易");
        EnterpriseCreditReportRaw.Summary summary = report.getSummary();

        int yearsIndex = indexOfContains(lines, "责任的年份");
        if (yearsIndex >= 0 && yearsIndex + 1 < lines.size()) {
            String[] parts = normalizeSpaces(lines.get(yearsIndex + 1)).split(" ");
            if (parts.length >= 4) {
                summary.setFirstCreditYear(parseInteger(parts[0]));
                summary.setCreditInstitutionCount(parseInteger(parts[1]));
                summary.setActiveCreditInstitutionCount(parseInteger(parts[2]));
                summary.setFirstRelatedRepaymentYear(parseInteger(parts[3]));
            }
        } else {
            report.getParseWarnings().add("未识别企业征信概要中的机构数信息");
        }

        Matcher balanceMatcher = ENTERPRISE_SUMMARY_BALANCES_PATTERN.matcher(normalized);
        if (balanceMatcher.find()) {
            summary.setLoanBalance(parseAmount(balanceMatcher.group(1)));
            summary.setGuaranteeBalance(parseAmount(balanceMatcher.group(2)));
            summary.setRecoveredLoanBalance(parseAmount(balanceMatcher.group(3)));
            summary.setGuaranteeConcernBalance(parseAmount(balanceMatcher.group(4)));
            summary.setLoanConcernBalance(parseAmount(balanceMatcher.group(5)));
            summary.setGuaranteeBadBalance(parseAmount(balanceMatcher.group(6)));
            summary.setLoanBadBalance(parseAmount(balanceMatcher.group(7)));
        } else {
            report.getParseWarnings().add("未识别企业征信概要中的余额信息");
        }

        Matcher publicMatcher = ENTERPRISE_PUBLIC_COUNTS_PATTERN.matcher(normalized);
        if (publicMatcher.find()) {
            summary.setNonCreditAccountCount(parseInteger(publicMatcher.group(1)));
            summary.setTaxArrearsCount(parseInteger(publicMatcher.group(2)));
            summary.setCivilJudgmentCount(parseInteger(publicMatcher.group(3)));
            summary.setEnforcementCount(parseInteger(publicMatcher.group(4)));
            summary.setAdminPenaltyCount(parseInteger(publicMatcher.group(5)));
        }

        Matcher shortTermMatcher = ENTERPRISE_SHORT_TERM_SUMMARY_PATTERN.matcher(normalized);
        if (shortTermMatcher.find()) {
            summary.setShortTermLoanAccountCount(parseInteger(shortTermMatcher.group(7)));
            summary.setShortTermLoanBalance(parseAmount(shortTermMatcher.group(8)));
        }

        Matcher guaranteeMatcher = ENTERPRISE_GUARANTEE_SUMMARY_PATTERN.matcher(normalized);
        if (guaranteeMatcher.find()) {
            summary.setOtherGuaranteeAccountCount(parseInteger(guaranteeMatcher.group(7)));
            summary.setOtherGuaranteeBalance(parseAmount(guaranteeMatcher.group(8)));
        }

        Matcher creditMatcher = ENTERPRISE_CREDIT_SUMMARY_PATTERN.matcher(normalized);
        if (creditMatcher.find()) {
            summary.setNonRevolvingCreditTotal(parseAmount(creditMatcher.group(1)));
            summary.setNonRevolvingCreditUsed(parseAmount(creditMatcher.group(2)));
            summary.setNonRevolvingCreditAvailable(parseAmount(creditMatcher.group(3)));
            summary.setRevolvingCreditTotal(parseAmount(creditMatcher.group(4)));
            summary.setRevolvingCreditUsed(parseAmount(creditMatcher.group(5)));
            summary.setRevolvingCreditAvailable(parseAmount(creditMatcher.group(6)));
        }
    }

    /**
     * 提取企业基本概况与相关还款责任概要。
     */
    private void populateEnterpriseBasicProfile(List<String> pageTexts, EnterpriseCreditReportRaw report) {
        if (pageTexts.size() < 4) {
            report.getParseWarnings().add("企业征信报告页数不足，无法提取基本概况信息");
            return;
        }
        String normalized = cleanupEnterprisePage(pageTexts.get(3));
        EnterpriseCreditReportRaw.BasicProfile basic = report.getBasicProfile();

        Matcher relatedMatcher = ENTERPRISE_RELATED_REPAYMENT_SUMMARY_PATTERN.matcher(normalized);
        if (relatedMatcher.find()) {
            report.getSummary().setRelatedRepaymentAmount(parseAmount(relatedMatcher.group(4)));
            report.getSummary().setRelatedRepaymentAccountCount(parseInteger(relatedMatcher.group(5)));
            report.getSummary().setRelatedRepaymentBalance(parseAmount(relatedMatcher.group(6)));
            report.getSummary().setRelatedRepaymentConcernBalance(parseAmount(relatedMatcher.group(7)));
            report.getSummary().setRelatedRepaymentBadBalance(parseAmount(relatedMatcher.group(8)));
        }

        basic.setEconomicType(extractEnterpriseBasicField(normalized, "经济类型"));
        basic.setOrganizationType(extractEnterpriseBasicField(normalized, "组织机构类型"));
        basic.setEnterpriseScale(extractEnterpriseBasicField(normalized, "企业规模"));
        basic.setIndustry(extractEnterpriseBasicField(normalized, "所属行业"));
        basic.setEstablishYear(parseInteger(extractEnterpriseBasicField(normalized, "成立年份")));
        basic.setCertificateValidUntil(extractEnterpriseBasicField(normalized, "登记证书有效截止日期"));
        basic.setRegisterAddress(extractEnterpriseBasicField(normalized, "登记地址"));
        basic.setOperatingAddress(extractEnterpriseBasicField(normalized, "办公/经营地址"));
        basic.setBusinessStatus(extractEnterpriseBasicField(normalized, "存续状态"));
        basic.setRegisteredCapital(parseAmount(extractFirst(normalized, ENTERPRISE_REGISTERED_CAPITAL_PATTERN)));
    }

    /**
     * 解析未结清贷款明细，用于识别企业信贷余额、抵押余额和当前逾期情况。
     */
    private void populateEnterpriseUnsettledLoans(List<String> pageTexts, EnterpriseCreditReportRaw report) {
        if (pageTexts.size() < 5) {
            report.getParseWarnings().add("企业征信报告页数不足，无法提取未结清贷款");
            return;
        }
        List<String> lines = cleanEnterpriseLines(pageTexts.get(4));
        int start = indexOfContains(lines, "短期借款 共");
        if (start < 0) {
            report.getParseWarnings().add("未识别未结清贷款章节");
            return;
        }

        int index = start + 1;
        while (index < lines.size()) {
            while (index < lines.size() && !isEnterpriseAccountToken(lines.get(index))) {
                index++;
            }
            if (index >= lines.size()) {
                break;
            }
            EnterpriseLoanParseResult parsed = parseEnterpriseUnsettledLoan(lines, report, index);
            if (parsed == null || parsed.loan() == null) {
                break;
            }
            report.getUnsettledLoans().add(parsed.loan());
            index = parsed.nextIndex();
        }
    }

    /**
     * 识别纳税信用等级，优先取企业信用报告中的认证记录。
     */
    private void populateEnterpriseTaxLevels(List<String> pageTexts, EnterpriseCreditReportRaw report) {
        for (String pageText : pageTexts) {
            String normalized = cleanupEnterprisePage(pageText);
            Matcher matcher = ENTERPRISE_TAX_LEVEL_PATTERN.matcher(normalized);
            while (matcher.find()) {
                String level = matcher.group(1);
                if (!report.getTaxCreditLevels().contains(level)) {
                    report.getTaxCreditLevels().add(level);
                }
            }
        }
    }

    /**
     * 解析一笔未结清贷款表格记录。
     */
    private EnterpriseLoanParseResult parseEnterpriseUnsettledLoan(List<String> lines,
                                                                   EnterpriseCreditReportRaw report,
                                                                   int startIndex) {
        int index = startIndex;
        StringBuilder accountNo = new StringBuilder();
        while (index < lines.size() && isEnterpriseAccountToken(lines.get(index))) {
            accountNo.append(lines.get(index));
            index++;
        }
        if (accountNo.isEmpty()) {
            return null;
        }

        StringBuilder institution = new StringBuilder();
        while (index < lines.size() && !ENTERPRISE_CURRENT_LOAN_LINE_PATTERN.matcher(lines.get(index)).find()) {
            institution.append(lines.get(index)).append(' ');
            index++;
        }
        if (index >= lines.size()) {
            report.getParseWarnings().add("未识别未结清贷款业务行: " + abbreviate(accountNo.toString()));
            return null;
        }

        Matcher coreMatcher = ENTERPRISE_CURRENT_LOAN_LINE_PATTERN.matcher(lines.get(index));
        if (!coreMatcher.find()) {
            report.getParseWarnings().add("未识别未结清贷款业务行: " + abbreviate(lines.get(index)));
            return null;
        }
        index++;
        if (index >= lines.size()) {
            report.getParseWarnings().add("未识别未结清贷款详情行: " + abbreviate(accountNo.toString()));
            return null;
        }

        Matcher detailMatcher = ENTERPRISE_CURRENT_LOAN_DETAIL_PATTERN.matcher(lines.get(index));
        if (!detailMatcher.find()) {
            report.getParseWarnings().add("未识别未结清贷款详情行: " + abbreviate(lines.get(index)));
            return null;
        }

        EnterpriseCreditReportRaw.UnsettledLoan loan = new EnterpriseCreditReportRaw.UnsettledLoan();
        loan.setAccountNo(accountNo.toString());
        loan.setInstitution(normalizeWrappedChinese(institution.toString()));
        loan.setBusinessType(coreMatcher.group(1));
        loan.setOpenDate(coreMatcher.group(2));
        loan.setDueDate(coreMatcher.group(3));
        loan.setCurrency(coreMatcher.group(4));
        loan.setLoanAmount(parseAmount(coreMatcher.group(5)));
        loan.setDisbursementForm(coreMatcher.group(6));
        loan.setGuaranteeType(normalizeWrappedChinese(detailMatcher.group(1)));
        loan.setBalance(parseAmount(detailMatcher.group(2)));
        loan.setFiveClass(detailMatcher.group(3));
        loan.setOverdueTotal(parseAmount(detailMatcher.group(4)));
        loan.setOverduePrincipal(parseAmount(detailMatcher.group(5)));
        loan.setOverdueMonths(parseEnterpriseOverdueMonths(detailMatcher.group(6)));
        loan.setLastRepaymentDate(detailMatcher.group(7));

        int nextIndex = index + 1;
        if (nextIndex < lines.size() && !isEnterpriseAccountToken(lines.get(nextIndex))) {
            Matcher tailMatcher = ENTERPRISE_CURRENT_LOAN_TAIL_PATTERN.matcher(lines.get(nextIndex));
            if (tailMatcher.find()) {
                loan.setLastRepaymentAmount(parseAmount(tailMatcher.group(1)));
                loan.setLastRepaymentForm(tailMatcher.group(2));
                String maybeReportDate = extractLastDate(lines.get(nextIndex));
                if (maybeReportDate != null) {
                    loan.setReportDate(maybeReportDate);
                }
                nextIndex++;
            }
        }
        while (nextIndex < lines.size() && !isEnterpriseAccountToken(lines.get(nextIndex))) {
            nextIndex++;
        }
        return new EnterpriseLoanParseResult(loan, nextIndex);
    }

    /**
     * 清理企业征信页级文本，去掉页脚和多余空白。
     */
    private String cleanupEnterprisePage(String text) {
        return normalizeSpaces(text
                .replace("\r", "\n")
                .replaceAll("第\\s*\\d+\\s*页/共\\s*\\d+\\s*页", " ")
                .replaceAll("第\\s*\\d+\\s*页\\s*/\\s*共\\s*\\d+\\s*页", " ")
                .replace('\n', ' '));
    }

    /**
     * 清理企业征信表格页的逐行文本。
     */
    private List<String> cleanEnterpriseLines(String pageText) {
        List<String> lines = new ArrayList<>();
        for (String line : pageText.replace("\r", "\n").split("\n")) {
            String normalized = normalizeSpaces(line);
            if (normalized.isBlank() || normalized.matches("第\\s*\\d+\\s*页/共\\s*\\d+\\s*页")) {
                continue;
            }
            lines.add(normalized);
        }
        return lines;
    }

    /**
     * 提取企业基本概况页中的单字段值。
     */
    private String extractEnterpriseBasicField(String normalizedPage, String fieldName) {
        Pattern pattern = Pattern.compile(String.format(ENTERPRISE_BASIC_FIELD_PATTERN_TEMPLATE, Pattern.quote(fieldName)));
        return trimToNull(normalizeWrappedChinese(extractFirst(normalizedPage, pattern)));
    }

    /**
     * 判断一行是否为企业征信表格中的账户编号片段。
     */
    private boolean isEnterpriseAccountToken(String line) {
        return line != null
                && line.matches("^[A-Z0-9X-]+$")
                && !line.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    /**
     * 查找包含指定关键字的首行索引。
     */
    private int indexOfContains(List<String> lines, String keyword) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(keyword)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 企业征信中的逾期月数字段兼容 N / 数值。
     */
    private Integer parseEnterpriseOverdueMonths(String value) {
        if (value == null || value.isBlank() || "N".equalsIgnoreCase(value) || "--".equals(value)) {
            return 0;
        }
        return parseInteger(value);
    }

    /**
     * 抽取行尾日期，用于信息报告日期。
     */
    private String extractLastDate(String text) {
        Matcher matcher = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})$").matcher(normalizeSpaces(text));
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 空白转 null，避免把空字符串落到结构化字段。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = normalizeSpaces(value);
        return normalized.isBlank() ? null : normalized;
    }

    /**
     * 征信报告中的金额字段统一转成 BigDecimal。
     */
    private BigDecimal parseAmount(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace(",", "")
                .replace("人民币元", "")
                .replace("万元", "")
                .replace("万", "")
                .trim();
        if (normalized.isBlank() || "--".equals(normalized)) {
            return null;
        }
        return new BigDecimal(normalized);
    }

    /**
     * 解析单条信用卡记录。
     */
    private PersonalCreditReportRaw.CreditCardAccount parseCreditCardBlock(String block) {
        Matcher matcher = CARD_ISSUE_PATTERN.matcher(block);
        if (!matcher.matches()) {
            return null;
        }

        PersonalCreditReportRaw.CreditCardAccount account = new PersonalCreditReportRaw.CreditCardAccount();
        account.setIssueDate(matcher.group(1));
        account.setInstitution(normalizeWrappedChinese(matcher.group(2)));
        account.setCardType(matcher.group(3));

        String accountInfo = matcher.group(4);
        account.setAccountCurrency(extractFirst(accountInfo, Pattern.compile("^([^，]+)")));
        account.setCardTailNumber(extractFirst(accountInfo, CARD_TAIL_PATTERN));

        String details = matcher.group(5);
        account.setAsOfMonth(extractFirst(details, Pattern.compile("截至(\\d{4}年\\d{2}月)")));
        account.setCreditLimit(parseAmount(extractFirst(details, CREDIT_LIMIT_PATTERN)));
        account.setBalance(parseAmount(extractFirst(details, BALANCE_PATTERN)));
        account.setCurrentOverdue(parseCurrentOverdue(details));
        account.setOverdueMonthsInLast5Years(parseInteger(extractFirst(details, OVERDUE_MONTHS_PATTERN)));
        account.setHasOverdue90Plus(parseOverdue90Plus(details));

        if (details.contains("尚未激活")) {
            account.setAccountStatus("NOT_ACTIVATED");
            account.setStatusMonth(firstMatch(details, MONTH_PATTERN));
        } else if (details.contains("销户")) {
            account.setAccountStatus("CLOSED");
            account.setStatusMonth(firstMatch(details, MONTH_PATTERN));
        } else {
            account.setAccountStatus("OPEN");
        }
        return account;
    }

    /**
     * 解析单条贷款记录。
     */
    private PersonalCreditReportRaw.LoanAccount parseLoanBlock(String block) {
        Matcher revolvingMatcher = REVOLVING_LOAN_PATTERN.matcher(block);
        if (revolvingMatcher.matches()) {
            PersonalCreditReportRaw.LoanAccount account = new PersonalCreditReportRaw.LoanAccount();
            account.setIssueDate(revolvingMatcher.group(1));
            account.setInstitution(normalizeWrappedChinese(revolvingMatcher.group(2)));
            account.setLoanType(revolvingMatcher.group(3).trim());
            account.setAccountMode("REVOLVING");
            account.setLimitValidUntil(revolvingMatcher.group(4));
            account.setRevolving("可循环使用".equals(revolvingMatcher.group(5)));
            account.setAsOfMonth(revolvingMatcher.group(6));
            account.setCreditLimit(parseAmount(revolvingMatcher.group(7)));
            account.setCurrency(revolvingMatcher.group(8));
            account.setBalance(parseAmount(revolvingMatcher.group(9)));
            account.setCurrentOverdue(parseCurrentOverdue(revolvingMatcher.group(10)));
            account.setCurrentStatus(parseLoanStatus(revolvingMatcher.group(10)));
            return account;
        }

        Matcher settledMatcher = ISSUED_LOAN_SETTLED_PATTERN.matcher(block);
        if (settledMatcher.matches()) {
            PersonalCreditReportRaw.LoanAccount account = new PersonalCreditReportRaw.LoanAccount();
            account.setIssueDate(settledMatcher.group(1));
            account.setInstitution(normalizeWrappedChinese(settledMatcher.group(2)));
            account.setAmountIssued(parseAmount(settledMatcher.group(3)));
            account.setCurrency(settledMatcher.group(4));
            account.setLoanType(settledMatcher.group(5).trim());
            account.setAccountMode("DISBURSED");
            account.setSettledMonth(settledMatcher.group(6));
            account.setCurrentStatus("SETTLED");
            account.setCurrentOverdue(false);
            account.setBalance(BigDecimal.ZERO);
            return account;
        }
        return null;
    }

    /**
     * 解析查询记录表格，并修复“资信审查”被换行拆开的情况。
     */
    private List<PersonalCreditReportRaw.QueryRecord> parseQueries(String section,
                                                                   List<String> warnings,
                                                                   String sectionName) {
        List<String> lines = cleanQuerySectionLines(section);
        List<String> merged = mergeQueryLines(lines);
        List<PersonalCreditReportRaw.QueryRecord> records = new ArrayList<>();
        for (String recordLine : merged) {
            Matcher matcher = QUERY_RECORD_PATTERN.matcher(recordLine);
            if (!matcher.matches()) {
                warnings.add("未识别" + sectionName + "记录: " + abbreviate(recordLine));
                continue;
            }
            String rest = matcher.group(3).trim();
            PersonalCreditReportRaw.QueryRecord record = new PersonalCreditReportRaw.QueryRecord();
            record.setIndex(parseInteger(matcher.group(1)));
            record.setQueryDate(matcher.group(2));
            String reason = extractQueryReason(rest);
            if (reason != null) {
                record.setQueryReason(reason);
                record.setInstitution(normalizeWrappedChinese(removeFlexibleSuffix(rest, reason)));
                records.add(record);
                continue;
            }
            if (rest.contains("法人代表、负责人、高管等资信审") && rest.endsWith("查")) {
                int reasonStart = rest.indexOf("法人代表、负责人、高管等资信审");
                String prefix = rest.substring(0, reasonStart);
                String suffix = rest.substring(reasonStart + "法人代表、负责人、高管等资信审".length(), rest.length() - 1);
                record.setQueryReason("法人代表、负责人、高管等资信审查");
                record.setInstitution(normalizeWrappedChinese(prefix + suffix));
                records.add(record);
                continue;
            }
            warnings.add("未识别" + sectionName + "原因: " + abbreviate(recordLine));
            continue;
        }
        records.sort(Comparator.comparing(PersonalCreditReportRaw.QueryRecord::getIndex, Comparator.nullsLast(Integer::compareTo)));
        return records;
    }

    /**
     * 从全文中提取指定章节文本。
     */
    private String extractSection(String text, Pattern pattern, String name) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group("content");
    }

    /**
     * 根据不同章节的块级正则，把长文本切成单条账户/查询记录。
     */
    private List<String> extractBlocks(String section, Pattern pattern) {
        List<String> blocks = new ArrayList<>();
        Matcher matcher = pattern.matcher(section);
        while (matcher.find()) {
            String block = matcher.group(1).trim();
            if (!block.isBlank()) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    /**
     * 清洗查询记录章节中的页脚、表头和空行。
     */
    private List<String> cleanQuerySectionLines(String section) {
        String noFooter = section
                .replace("\r", "\n")
                .replaceAll("第\\s*\\d+\\s*页，共\\s*\\d+\\s*页", " ")
                .replaceAll("编号\\s+查询日期\\s+查询机构\\s+查询原因", " ");
        List<String> lines = new ArrayList<>();
        for (String line : noFooter.split("\n")) {
            String trimmed = normalizeSpaces(line);
            if (trimmed.isBlank()) {
                continue;
            }
            if (trimmed.matches("\\d+\\.")) {
                continue;
            }
            lines.add(trimmed);
        }
        return lines;
    }

    /**
     * 把被 PDF 换行拆开的查询记录重新拼成一行。
     */
    private List<String> mergeQueryLines(List<String> lines) {
        List<String> records = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.matches("^\\d+\\s+\\d{4}年\\d{2}月\\d{2}日\\s+.*")) {
                if (!current.isEmpty()) {
                    records.add(current.toString().trim());
                }
                current = new StringBuilder(line);
            } else if (!current.isEmpty()) {
                current.append(' ').append(line);
            }
        }
        if (!current.isEmpty()) {
            records.add(current.toString().trim());
        }
        return records;
    }

    /**
     * 从查询记录尾部识别查询原因，允许原因文本中存在断行空格。
     */
    private String extractQueryReason(String rest) {
        String compact = rest.replace(" ", "");
        return QUERY_REASONS.stream()
                .filter(reason -> compact.contains(reason.replace(" ", "")))
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    /**
     * 清洗叙述类章节中的页码、编号和换行噪音。
     */
    private String cleanupNarrativeSection(String text) {
        return normalizeSpaces(text
                .replace("\r", "\n")
                .replaceAll("报告编号：[^\\n]+", " ")
                .replaceAll("姓名：[^\\n]+", " ")
                .replaceAll("第\\s*\\d+\\s*页，共\\s*\\d+\\s*页", " ")
                .replaceAll("(?<!\\d)(\\d+)\\.", " ")
                .replace('\n', ' '));
    }

    /**
     * 标准空白归一化，便于后续正则匹配。
     */
    private String normalizeSpaces(String value) {
        return value == null ? "" : value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    /**
     * 去掉中文之间被 PDF 强行插入的空格，避免机构名和企业名被拆断。
     */
    private String normalizeWrappedChinese(String value) {
        return normalizeSpaces(value).replaceAll("(?<=[\\p{IsHan}])\\s+(?=[\\p{IsHan}])", "");
    }

    /**
     * 兼容删除尾部原因文本时的断行空格差异。
     */
    private String removeFlexibleSuffix(String text, String suffix) {
        String regex = suffix.chars()
                .mapToObj(ch -> Pattern.quote(String.valueOf((char) ch)) + "\\s*")
                .reduce("", String::concat);
        return text.replaceFirst(regex + "$", "").trim();
    }

    /**
     * 将贷款状态文本统一成可比较的状态码。
     */
    private String parseLoanStatus(String rawStatus) {
        String normalized = normalizeSpaces(rawStatus);
        if (normalized.contains("无逾期")) {
            return "NORMAL";
        }
        if (normalized.contains("逾期")) {
            return "OVERDUE";
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    /**
     * 从文本中识别“当前是否逾期”。
     */
    private Boolean parseCurrentOverdue(String text) {
        String normalized = normalizeSpaces(text);
        if (normalized.contains("当前无逾期")) {
            return false;
        }
        if (normalized.contains("当前逾期")) {
            return true;
        }
        return null;
    }

    /**
     * 从文本中识别是否出现过 90 天以上逾期。
     */
    private Boolean parseOverdue90Plus(String text) {
        String normalized = normalizeSpaces(text);
        if (normalized.contains("没有发生过90天以上逾期")) {
            return false;
        }
        if (normalized.contains("发生过90天以上逾期")) {
            return true;
        }
        return null;
    }

    /**
     * 返回首个匹配组，用于月份、日期等简单字段提取。
     */
    private String firstMatch(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 提取首个捕获组，不命中时返回 null。
     */
    private String extractFirst(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 金额、次数等基础整型解析。
     */
    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value.replace(",", "").trim());
    }

    private record EnterpriseLoanParseResult(EnterpriseCreditReportRaw.UnsettledLoan loan, int nextIndex) {
    }

    /**
     * 缩短告警内容，避免 parseWarnings 里塞入整段原文。
     */
    private String abbreviate(String text) {
        String normalized = normalizeSpaces(text);
        if (normalized.length() <= 120) {
            return normalized;
        }
        return normalized.substring(0, 120) + "...";
    }

}
