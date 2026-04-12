package com.qhr.service.impl;

import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.service.CreditReportParseService;
import com.qhr.service.WeixinCloudFileService;
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
            if (!rawText.contains("个人信用报告")) {
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

    /**
     * 征信报告中的金额字段统一转成 BigDecimal。
     */
    private BigDecimal parseAmount(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace(",", "").trim();
        if (normalized.isBlank() || "--".equals(normalized)) {
            return null;
        }
        return new BigDecimal(normalized);
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
