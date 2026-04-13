package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.qhr.model.Enterprise;
import com.qhr.service.ApplicantProfileAssembler;
import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.credit.EnterpriseCreditReportRaw;
import com.qhr.vo.credit.PersonalCreditReportRaw;
import com.qhr.vo.match.ApplicationContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 统一画像组装器。
 * 第一版先覆盖 QCC 财税样例数据与个人征信解析结果中的高频字段；企业征信待接口落地后再补。
 */
@ApplicationScoped
public class ApplicantProfileAssemblerImpl implements ApplicantProfileAssembler {

    private static final DateTimeFormatter ENTERPRISE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CREDIT_REPORT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter CREDIT_QUERY_DATE = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    @Override
    public ApplicantProfile assemble(Enterprise enterprise,
                                     JsonNode companyDetail,
                                     JsonNode taxData,
                                     PersonalCreditReportRaw personalCreditReport,
                                     EnterpriseCreditReportRaw enterpriseCreditData,
                                     ApplicationContext applicationContext) {
        ApplicantProfile profile = new ApplicantProfile();
        profile.setFinancialReport(new ApplicantProfile.FinancialReport());

        populateEnterprise(profile, enterprise);
        populateCompanyDetail(profile, companyDetail, enterprise);
        populateTax(profile, taxData);
        populatePersonalCredit(profile, personalCreditReport);
        populateEnterpriseCredit(profile, enterpriseCreditData);
        populateContextDerivedFacts(profile, applicationContext);
        populateDerivedMetrics(profile);
        return profile;
    }

    private void populateEnterprise(ApplicantProfile profile, Enterprise enterprise) {
        if (enterprise == null) {
            return;
        }
        profile.setCompanyName(enterprise.getName());
        profile.setUnifiedSocialCreditCode(enterprise.getCreditCode());
        profile.setRegisterAddress(enterprise.getAddress());
        profile.setCompanyRegion(extractRegion(enterprise.getAddress()));
        profile.setCompanyStatus(enterprise.getStatus());

        LocalDate establishDate = parseLocalDate(enterprise.getStartDate());
        profile.setEstablishDate(establishDate);
        if (establishDate != null) {
            Period period = Period.between(establishDate, LocalDate.now());
            profile.setCompanyAge(period.getYears());
            profile.setCompanyAgeMonths(period.getYears() * 12 + period.getMonths());
        }
    }

    private void populateCompanyDetail(ApplicantProfile profile, JsonNode companyDetail, Enterprise enterprise) {
        JsonNode detail = unwrapCompanyDetail(companyDetail);
        if (detail == null || detail.isMissingNode() || detail.isNull()) {
            return;
        }

        setIfHasText(detail.path("Name"), profile::setCompanyName);
        setIfHasText(detail.path("CreditCode"), profile::setUnifiedSocialCreditCode);
        setIfHasText(detail.path("Address"), profile::setRegisterAddress);
        setIfHasText(detail.path("Status"), profile::setCompanyStatus);

        String region = extractRegion(detail.path("Area"));
        if (region == null) {
            region = extractRegion(detail.path("Address").asText(null));
        }
        if (region != null) {
            profile.setCompanyRegion(region);
        }

        LocalDate establishDate = parseLocalDate(detail.path("StartDate").asText(null));
        if (establishDate == null && enterprise != null) {
            establishDate = parseLocalDate(enterprise.getStartDate());
        }
        if (establishDate != null) {
            profile.setEstablishDate(establishDate);
            Period period = Period.between(establishDate, LocalDate.now());
            profile.setCompanyAge(period.getYears());
            profile.setCompanyAgeMonths(period.getYears() * 12 + period.getMonths());
        }

        JsonNode industry = detail.path("Industry");
        setIfHasText(industry.path("Industry"), profile::setIndustry);
        List<String> industryTags = new ArrayList<>();
        addIfHasText(industryTags, industry.path("Industry").asText(null));
        addIfHasText(industryTags, industry.path("SubIndustry").asText(null));
        addIfHasText(industryTags, industry.path("MiddleCategory").asText(null));
        addIfHasText(industryTags, industry.path("SmallCategory").asText(null));
        if (!industryTags.isEmpty()) {
            profile.setIndustryTags(industryTags);
        }

        BigDecimal registeredCapital = decimalOrNull(detail.path("RegisteredCapital").asText(null));
        if (registeredCapital != null) {
            profile.setRegisteredCapital(registeredCapital);
        }
        BigDecimal paidInCapital = decimalOrNull(detail.path("PaidUpCapital").asText(null));
        if (paidInCapital != null) {
            profile.setPaidInCapital(paidInCapital);
        }

        String legalRepName = detail.path("OperName").asText(null);
        BigDecimal legalRepShareRatio = extractLegalRepShareRatio(detail.path("Partners"), legalRepName);
        if (legalRepShareRatio != null) {
            profile.setLegalPersonShareRatio(legalRepShareRatio);
        }

        ChangeStats changeStats = extractLegalRepChangeStats(detail.path("ChangeRecords"));
        if (changeStats.count2y() != null) {
            profile.setLegalPersonChangeCount(changeStats.count2y());
            profile.setLegalPersonChangeCount2y(changeStats.count2y());
        }
        if (changeStats.lastGapMonths() != null) {
            profile.setLegalPersonLastChangeGapMonths(changeStats.lastGapMonths());
        }

        TagExtraction tagExtraction = extractEnterpriseTags(detail.path("TagList"));
        if (!tagExtraction.names().isEmpty()) {
            profile.setEnterpriseTags(String.join(",", tagExtraction.names()));
        }
        if (!tagExtraction.codes().isEmpty()) {
            profile.setEnterpriseTagCodes(new ArrayList<>(tagExtraction.codes()));
        }
    }

    private void populateTax(ApplicantProfile profile, JsonNode taxData) {
        JsonNode data = unwrapTaxData(taxData);
        if (data == null || data.isMissingNode() || data.isNull()) {
            return;
        }

        Map<Integer, YearAggregate> invoiceByYear = extractYearAggregate(data.path("SaleList"), "Amount");
        applyYearAggregates(invoiceByYear, profile::setInvoiceAmountCurrentYear, profile::setInvoiceAmountLastYear,
                profile::setInvoiceAmount2025, profile::setInvoiceAmount2024, profile::setInvoiceAmount2023,
                profile::setInvoiceAmount2022);
        Integer latestInvoiceYear = latestYear(invoiceByYear);
        if (latestInvoiceYear != null) {
            YearAggregate latestInvoice = invoiceByYear.get(latestInvoiceYear);
            profile.setAnnualInvoiceAmount(latestInvoice.amount());
            profile.setInvoiceAmount12m(latestInvoice.amount());
            profile.setInvoiceMonths12m(latestInvoice.positiveMonthCount());
            profile.setMonthlyInvoiceAmounts(new ArrayList<>(latestInvoice.monthlyAmounts()));
        }

        Map<Integer, YearAggregate> totalTaxByYear = extractYearAggregate(data.path("TaxData").path("TotalTaxList"), "Amount");
        applyYearAggregates(totalTaxByYear, profile::setTaxAmountCurrentYear, profile::setTaxAmountLastYear,
                profile::setTaxAmount2025, profile::setTaxAmount2024, profile::setTaxAmount2023,
                profile::setTaxAmount2022);
        Integer latestTaxYear = latestYear(totalTaxByYear);
        if (latestTaxYear != null) {
            YearAggregate latestTax = totalTaxByYear.get(latestTaxYear);
            profile.setAnnualTaxAmount(latestTax.amount());
            profile.setTaxAmount12m(latestTax.amount());
            profile.setTaxMonths12m(latestTax.positiveMonthCount());
            profile.setTaxMonths(new ArrayList<>(latestTax.positiveMonths()));
            profile.setTaxMonths24m(sumPositiveMonths(totalTaxByYear, latestTaxYear, 2));
        }

        Map<Integer, BigDecimal> taxBurdenByYear = extractTaxBurdenRateByYear(data.path("TaxBurdenRateList"));
        Integer latestBurdenYear = latestYear(taxBurdenByYear);
        if (latestBurdenYear != null) {
            profile.setTaxRate(taxBurdenByYear.get(latestBurdenYear));
            profile.setTaxBurdenRate1y(taxBurdenByYear.get(latestBurdenYear));
        }
        profile.setTaxBurdenRate2025(taxBurdenByYear.get(2025));
        profile.setTaxBurdenRate2024(taxBurdenByYear.get(2024));
        if (profile.getTaxBurdenRate2025() != null && profile.getTaxBurdenRate2024() != null
                && profile.getTaxBurdenRate2024().compareTo(BigDecimal.ZERO) > 0) {
            profile.setTaxBurdenRateRatio25vs24(profile.getTaxBurdenRate2025()
                    .divide(profile.getTaxBurdenRate2024(), 4, RoundingMode.HALF_UP));
        }

        JsonNode corporateDeclareList = data.path("DeclarationDetail").path("CorporateInTaxDeclareList");
        if (corporateDeclareList.isArray() && !corporateDeclareList.isEmpty()) {
            JsonNode latestDeclare = corporateDeclareList.get(corporateDeclareList.size() - 1);
            profile.setProfitCurrentYear(decimalOrNull(latestDeclare.path("ThisYearCumulativeProfit").asText(null)));
            profile.getFinancialReport().setAnnualRevenue(decimalOrNull(latestDeclare.path("ThisYearSaleRevenue").asText(null)));
            profile.getFinancialReport().setAnnualProfit(profile.getProfitCurrentYear());
        }

        profile.setTotalTaxDeclarationCount(countDeclarationRows(data.path("DeclarationDetail")));
    }

    private void populatePersonalCredit(ApplicantProfile profile, PersonalCreditReportRaw report) {
        if (report == null) {
            return;
        }

        profile.setMaritalStatus(report.getPerson().getMaritalStatus());
        profile.setAge(parseAgeFromIdNo(report.getPerson().getIdNo()));
        profile.setCreditCardCount(size(report.getCreditCards()));
        profile.setLoanInstitutionCount(countDistinctInstitutions(report.getLoans()));
        profile.setPersonalLoanInstitutionCount(profile.getLoanInstitutionCount());

        BigDecimal creditCardLimitTotal = sumCreditCardLimit(report.getCreditCards());
        BigDecimal creditCardBalanceTotal = sumCreditCardBalance(report.getCreditCards());
        BigDecimal activeLoanBalanceTotal = sumLoanBalance(report.getLoans());
        BigDecimal guaranteeBalanceTotal = sumRelatedLiability(report.getRelatedLiabilities());

        profile.setGuaranteeExposure(guaranteeBalanceTotal);
        profile.setPersonalLiabilityExcludingGuarantee(activeLoanBalanceTotal.add(creditCardBalanceTotal));
        profile.setPersonalLiabilityIncludingGuarantee(profile.getPersonalLiabilityExcludingGuarantee().add(guaranteeBalanceTotal));
        profile.setPersonalLiability(profile.getPersonalLiabilityIncludingGuarantee());
        profile.setCreditLoan(sumLoanBalanceByType(report.getLoans(), false));
        profile.setMortgageLoan(sumLoanBalanceByType(report.getLoans(), true));
        profile.setPersonalCreditLoanDebt(profile.getCreditLoan());

        BigDecimal utilization = percentage(creditCardBalanceTotal, creditCardLimitTotal);
        profile.setCreditCardUsageRate(utilization);
        profile.setCreditCardUtilizationCurrent(utilization);

        LocalDate baseDate = resolveCreditBaseDate(report);
        profile.setReviewQueryCount2w(countQueriesByDays(report.getInstitutionQueries(), baseDate, 14, null));
        profile.setReviewQueryCount1m(countQueries(report.getInstitutionQueries(), baseDate, 1, null));
        profile.setReviewQueryCount2m(countQueries(report.getInstitutionQueries(), baseDate, 2, null));
        profile.setReviewQueryCount3m(countQueries(report.getInstitutionQueries(), baseDate, 3, null));
        profile.setReviewQueryCount6m(countQueries(report.getInstitutionQueries(), baseDate, 6, null));
        profile.setReviewQueryCount12m(countQueries(report.getInstitutionQueries(), baseDate, 12, null));
        profile.setLoanOrCardQueryCount1m(countQueries(report.getInstitutionQueries(), baseDate, 1, Set.of("贷款审批", "信用卡审批")));
        profile.setLoanOrCardQueryCount12m(countQueries(report.getInstitutionQueries(), baseDate, 12, Set.of("贷款审批", "信用卡审批")));
        profile.setLoanQueryCount1m(countQueries(report.getInstitutionQueries(), baseDate, 1, Set.of("贷款审批")));
        profile.setLoanQueryCount4m(countQueries(report.getInstitutionQueries(), baseDate, 4, Set.of("贷款审批")));
        profile.setCreditCardQueryCount1m(countQueries(report.getInstitutionQueries(), baseDate, 1, Set.of("信用卡审批")));
        profile.setCreditInquiryCount(profile.getReviewQueryCount6m());

        int overdueTerms5y = report.getCreditCards().stream()
                .map(PersonalCreditReportRaw.CreditCardAccount::getOverdueMonthsInLast5Years)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        int maxOverdueTerms5y = report.getCreditCards().stream()
                .map(PersonalCreditReportRaw.CreditCardAccount::getOverdueMonthsInLast5Years)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0);
        boolean hasCurrentOverdue = report.getCreditCards().stream().anyMatch(card -> Boolean.TRUE.equals(card.getCurrentOverdue()))
                || report.getLoans().stream().anyMatch(loan -> Boolean.TRUE.equals(loan.getCurrentOverdue()));
        boolean hasHistoryOverdue = overdueTerms5y > 0
                || defaultZero(report.getSummary().getOverdueCreditCardAccountCount()) > 0
                || defaultZero(report.getSummary().getOverdue90PlusCreditCardAccountCount()) > 0;

        profile.setHasOverdue(hasCurrentOverdue || hasHistoryOverdue);
        profile.setOverdueTerms5y(overdueTerms5y == 0 ? null : overdueTerms5y);
        profile.setMaxConsecutiveOverdueTerms5y(maxOverdueTerms5y == 0 ? null : maxOverdueTerms5y);
        profile.setPersonalConsumerLoanCount(countLoansByKeyword(report.getLoans(), "消费"));
        profile.setPersonalOnlineSmallLoanCount(countSmallLoans(report.getLoans()));
        profile.setPersonalUnsecuredLoanInstitutionCount(countDistinctUnsecuredLoanInstitutions(report.getLoans()));
        profile.setAbnormalCreditAccount(hasAbnormalCreditAccount(report));
    }

    private void populateEnterpriseCredit(ApplicantProfile profile, EnterpriseCreditReportRaw enterpriseCreditData) {
        if (enterpriseCreditData == null) {
            return;
        }
        EnterpriseCreditReportRaw.Summary summary = enterpriseCreditData.getSummary();
        profile.setEnterpriseLoanInstitutionCount(firstNonNull(
                summary.getActiveCreditInstitutionCount(),
                summary.getCreditInstitutionCount()));

        BigDecimal enterpriseCreditLoan = BigDecimal.ZERO;
        BigDecimal enterpriseMortgageLiability = BigDecimal.ZERO;
        int overdueCount = 0;
        int maxOverdueMonths = 0;
        boolean abnormal = false;
        for (EnterpriseCreditReportRaw.UnsettledLoan loan : enterpriseCreditData.getUnsettledLoans()) {
            BigDecimal balance = defaultDecimal(loan.getBalance());
            if (containsIgnoreCase(loan.getGuaranteeType(), "抵押")) {
                enterpriseMortgageLiability = enterpriseMortgageLiability.add(balance);
            } else {
                enterpriseCreditLoan = enterpriseCreditLoan.add(balance);
            }

            boolean overdue = defaultDecimal(loan.getOverdueTotal()).compareTo(BigDecimal.ZERO) > 0
                    || defaultDecimal(loan.getOverduePrincipal()).compareTo(BigDecimal.ZERO) > 0
                    || defaultZero(loan.getOverdueMonths()) > 0;
            if (overdue) {
                overdueCount++;
                maxOverdueMonths = Math.max(maxOverdueMonths, defaultZero(loan.getOverdueMonths()));
            }
            if (!isNormalCreditClass(loan.getFiveClass())) {
                abnormal = true;
            }
        }

        BigDecimal enterpriseGuaranteeLiability = defaultDecimal(summary.getGuaranteeBalance())
                .add(defaultDecimal(summary.getRelatedRepaymentBalance()));
        profile.setEnterpriseCreditLoan(enterpriseCreditLoan);
        profile.setEnterpriseMortgageLiability(enterpriseMortgageLiability);
        profile.setEnterpriseGuaranteeLiability(enterpriseGuaranteeLiability);
        profile.setEnterpriseLiability(enterpriseCreditLoan
                .add(enterpriseMortgageLiability)
                .add(enterpriseGuaranteeLiability));
        profile.setEnterpriseOverdueCount12m(overdueCount);
        profile.setEnterpriseMaxOverdueMonths24m(maxOverdueMonths);

        abnormal = abnormal
                || defaultDecimal(summary.getLoanConcernBalance()).compareTo(BigDecimal.ZERO) > 0
                || defaultDecimal(summary.getLoanBadBalance()).compareTo(BigDecimal.ZERO) > 0
                || defaultDecimal(summary.getGuaranteeConcernBalance()).compareTo(BigDecimal.ZERO) > 0
                || defaultDecimal(summary.getGuaranteeBadBalance()).compareTo(BigDecimal.ZERO) > 0
                || defaultZero(summary.getTaxArrearsCount()) > 0
                || defaultZero(summary.getCivilJudgmentCount()) > 0
                || defaultZero(summary.getEnforcementCount()) > 0
                || defaultZero(summary.getAdminPenaltyCount()) > 0;
        profile.setEnterpriseAbnormalCreditAccount(abnormal);

        if ((profile.getTaxLevel() == null || profile.getTaxLevel().isBlank())
                && enterpriseCreditData.getTaxCreditLevels() != null
                && !enterpriseCreditData.getTaxCreditLevels().isEmpty()) {
            profile.setTaxLevel(enterpriseCreditData.getTaxCreditLevels().get(0));
        }
    }

    private void populateContextDerivedFacts(ApplicantProfile profile, ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        profile.setLegalRepCanJointLiability(applicationContext.getLegalRepJointLiabilityAvailable());
        profile.setShareholderCanJointLiability(applicationContext.getShareholderJointLiabilityAvailable());
        profile.setSpouseCanJointLiability(applicationContext.getSpouseJointLiabilityAvailable());
    }

    private void populateDerivedMetrics(ApplicantProfile profile) {
        if (profile.getInvoiceAmountCurrentYear() != null && profile.getInvoiceAmountLastYear() != null
                && profile.getInvoiceAmountLastYear().compareTo(BigDecimal.ZERO) > 0) {
            profile.setInvoiceGrowthRate(profile.getInvoiceAmountCurrentYear()
                    .subtract(profile.getInvoiceAmountLastYear())
                    .divide(profile.getInvoiceAmountLastYear(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
            profile.setInvoiceRatioLast2FullYears(profile.getInvoiceAmountCurrentYear()
                    .divide(profile.getInvoiceAmountLastYear(), 4, RoundingMode.HALF_UP));
        }

        if (profile.getFinancialReport() != null
                && profile.getFinancialReport().getAnnualRevenue() != null
                && profile.getFinancialReport().getAnnualRevenue().compareTo(BigDecimal.ZERO) > 0
                && profile.getProfitCurrentYear() != null) {
            profile.setProfitRate(profile.getProfitCurrentYear()
                    .divide(profile.getFinancialReport().getAnnualRevenue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
        }

        if (profile.getEnterpriseLiability() != null && profile.getInvoiceAmountLastYear() != null
                && profile.getInvoiceAmountLastYear().compareTo(BigDecimal.ZERO) > 0) {
            profile.setEnterpriseDebtToLastYearInvoiceRatio(profile.getEnterpriseLiability()
                    .divide(profile.getInvoiceAmountLastYear(), 4, RoundingMode.HALF_UP));
        }

        BigDecimal personalInclGuarantee = profile.getPersonalLiabilityIncludingGuarantee();
        if (personalInclGuarantee != null) {
            BigDecimal enterpriseLiability = defaultDecimal(profile.getEnterpriseLiability());
            profile.setCombinedDebtTotalInclGuaranteeCard(personalInclGuarantee.add(enterpriseLiability));
        }
        if (profile.getCombinedDebtTotalInclGuaranteeCard() != null && profile.getInvoiceAmountLastYear() != null
                && profile.getInvoiceAmountLastYear().compareTo(BigDecimal.ZERO) > 0) {
            profile.setCombinedDebtToLastYearInvoiceRatio(profile.getCombinedDebtTotalInclGuaranteeCard()
                    .divide(profile.getInvoiceAmountLastYear(), 4, RoundingMode.HALF_UP));
        }
        if (profile.getPersonalLiabilityIncludingGuarantee() != null && profile.getInvoiceAmountLastYear() != null
                && profile.getInvoiceAmountLastYear().compareTo(BigDecimal.ZERO) > 0) {
            profile.setPersonalDebtToLastYearInvoiceRatio(profile.getPersonalLiabilityIncludingGuarantee()
                    .divide(profile.getInvoiceAmountLastYear(), 4, RoundingMode.HALF_UP));
        }
        profile.setCombinedCreditLoanDebt(defaultDecimal(profile.getEnterpriseCreditLoan()).add(defaultDecimal(profile.getCreditLoan())));
        profile.setCombinedMortgageDebt(defaultDecimal(profile.getEnterpriseMortgageLiability()).add(defaultDecimal(profile.getMortgageLoan())));
    }

    private JsonNode unwrapTaxData(JsonNode taxData) {
        if (taxData == null || taxData.isNull() || taxData.isMissingNode()) {
            return null;
        }
        if (taxData.has("Data")) {
            return taxData.path("Data");
        }
        return taxData;
    }

    private JsonNode unwrapCompanyDetail(JsonNode companyDetail) {
        if (companyDetail == null || companyDetail.isNull() || companyDetail.isMissingNode()) {
            return null;
        }
        if (companyDetail.has("Result")) {
            return companyDetail.path("Result");
        }
        return companyDetail;
    }

    private Map<Integer, YearAggregate> extractYearAggregate(JsonNode listNode, String amountField) {
        Map<Integer, YearAggregate> result = new LinkedHashMap<>();
        if (!listNode.isArray()) {
            return result;
        }
        for (JsonNode yearNode : listNode) {
            Integer year = integerOrNull(yearNode.path("Year").asText(null));
            if (year == null) {
                continue;
            }
            JsonNode dataList = yearNode.path("DataList");
            BigDecimal total = BigDecimal.ZERO;
            List<BigDecimal> monthlyAmounts = new ArrayList<>();
            List<Integer> positiveMonths = new ArrayList<>();
            if (dataList.isArray()) {
                for (JsonNode monthNode : dataList) {
                    BigDecimal amount = decimalOrZero(monthNode.path(amountField).asText(null));
                    total = total.add(amount);
                    monthlyAmounts.add(amount);
                    Integer month = integerOrNull(monthNode.path("Month").asText(null));
                    if (month != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                        positiveMonths.add(month);
                    }
                }
            }
            result.put(year, new YearAggregate(total, monthlyAmounts, positiveMonths));
        }
        return result;
    }

    private Map<Integer, BigDecimal> extractTaxBurdenRateByYear(JsonNode listNode) {
        Map<Integer, BigDecimal> result = new LinkedHashMap<>();
        if (!listNode.isArray()) {
            return result;
        }
        for (JsonNode rateGroup : listNode) {
            JsonNode dataList = rateGroup.path("DataList");
            if (!dataList.isArray()) {
                continue;
            }
            for (JsonNode dataNode : dataList) {
                Integer year = integerOrNull(dataNode.path("Year").asText(null));
                BigDecimal ratio = percentageStringOrNull(dataNode.path("Ratio").asText(null));
                if (year != null && ratio != null) {
                    result.put(year, ratio);
                }
            }
        }
        return result;
    }

    private Integer countDeclarationRows(JsonNode declarationDetail) {
        if (declarationDetail == null || declarationDetail.isMissingNode() || declarationDetail.isNull()) {
            return null;
        }
        int count = 0;
        count += size(declarationDetail.path("CorporateInTaxDeclareList"));
        count += size(declarationDetail.path("ValueAddedTaxDeclareList"));
        count += size(declarationDetail.path("OtherTaxDeclareList"));
        return count == 0 ? null : count;
    }

    private void applyYearAggregates(Map<Integer, YearAggregate> aggregates,
                                     DecimalSetter currentYearSetter,
                                     DecimalSetter lastYearSetter,
                                     DecimalSetter setter2025,
                                     DecimalSetter setter2024,
                                     DecimalSetter setter2023,
                                     DecimalSetter setter2022) {
        Integer latestYear = latestYear(aggregates);
        if (latestYear != null) {
            currentYearSetter.accept(aggregates.get(latestYear).amount());
            YearAggregate lastYear = aggregates.get(latestYear - 1);
            if (lastYear != null) {
                lastYearSetter.accept(lastYear.amount());
            }
        }
        setIfPresent(aggregates, 2025, setter2025);
        setIfPresent(aggregates, 2024, setter2024);
        setIfPresent(aggregates, 2023, setter2023);
        setIfPresent(aggregates, 2022, setter2022);
    }

    private void setIfPresent(Map<Integer, YearAggregate> aggregates, int year, DecimalSetter setter) {
        YearAggregate aggregate = aggregates.get(year);
        if (aggregate != null) {
            setter.accept(aggregate.amount());
        }
    }

    private Integer latestYear(Map<Integer, ?> byYear) {
        return byYear.keySet().stream().max(Integer::compareTo).orElse(null);
    }

    private Integer sumPositiveMonths(Map<Integer, YearAggregate> aggregates, int latestYear, int spanYears) {
        int count = 0;
        for (int year = latestYear; year > latestYear - spanYears; year--) {
            YearAggregate aggregate = aggregates.get(year);
            if (aggregate != null) {
                count += aggregate.positiveMonthCount();
            }
        }
        return count == 0 ? null : count;
    }

    private BigDecimal sumCreditCardLimit(List<PersonalCreditReportRaw.CreditCardAccount> cards) {
        return cards.stream()
                .map(PersonalCreditReportRaw.CreditCardAccount::getCreditLimit)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCreditCardBalance(List<PersonalCreditReportRaw.CreditCardAccount> cards) {
        return cards.stream()
                .map(PersonalCreditReportRaw.CreditCardAccount::getBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumLoanBalance(List<PersonalCreditReportRaw.LoanAccount> loans) {
        return loans.stream()
                .filter(this::isActiveLoan)
                .map(PersonalCreditReportRaw.LoanAccount::getBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumLoanBalanceByType(List<PersonalCreditReportRaw.LoanAccount> loans, boolean mortgage) {
        return loans.stream()
                .filter(this::isActiveLoan)
                .filter(loan -> mortgage == isMortgageLoan(loan))
                .map(PersonalCreditReportRaw.LoanAccount::getBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumRelatedLiability(List<PersonalCreditReportRaw.RelatedRepaymentLiability> liabilities) {
        return liabilities.stream()
                .map(item -> item.getLoanBalance() != null ? item.getLoanBalance() : item.getLiabilityAmount())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int countDistinctInstitutions(List<PersonalCreditReportRaw.LoanAccount> loans) {
        Set<String> institutions = new HashSet<>();
        for (PersonalCreditReportRaw.LoanAccount loan : loans) {
            if (!isActiveLoan(loan) || loan.getInstitution() == null || loan.getInstitution().isBlank()) {
                continue;
            }
            institutions.add(loan.getInstitution().trim());
        }
        return institutions.size();
    }

    private int countDistinctUnsecuredLoanInstitutions(List<PersonalCreditReportRaw.LoanAccount> loans) {
        Set<String> institutions = new HashSet<>();
        for (PersonalCreditReportRaw.LoanAccount loan : loans) {
            if (!isActiveLoan(loan) || isMortgageLoan(loan) || loan.getInstitution() == null || loan.getInstitution().isBlank()) {
                continue;
            }
            institutions.add(loan.getInstitution().trim());
        }
        return institutions.size();
    }

    private int countLoansByKeyword(List<PersonalCreditReportRaw.LoanAccount> loans, String keyword) {
        return (int) loans.stream()
                .filter(this::isActiveLoan)
                .filter(loan -> containsIgnoreCase(loan.getLoanType(), keyword) || containsIgnoreCase(loan.getAccountMode(), keyword))
                .count();
    }

    private int countSmallLoans(List<PersonalCreditReportRaw.LoanAccount> loans) {
        return (int) loans.stream()
                .filter(this::isActiveLoan)
                .filter(loan -> containsAny(loan.getInstitution(), "小额贷款", "小贷", "消费金融", "网络", "网贷"))
                .count();
    }

    private int countQueries(List<PersonalCreditReportRaw.QueryRecord> queries,
                             LocalDate baseDate,
                             int months,
                             Set<String> reasonWhitelist) {
        LocalDate cutoff = baseDate.minusMonths(months);
        int count = 0;
        for (PersonalCreditReportRaw.QueryRecord query : queries) {
            LocalDate queryDate = parseCreditQueryDate(query.getQueryDate());
            if (queryDate == null || queryDate.isBefore(cutoff)) {
                continue;
            }
            if (reasonWhitelist != null && !reasonWhitelist.contains(query.getQueryReason())) {
                continue;
            }
            count++;
        }
        return count;
    }

    private int countQueriesByDays(List<PersonalCreditReportRaw.QueryRecord> queries,
                                   LocalDate baseDate,
                                   int days,
                                   Set<String> reasonWhitelist) {
        LocalDate cutoff = baseDate.minusDays(days);
        int count = 0;
        for (PersonalCreditReportRaw.QueryRecord query : queries) {
            LocalDate queryDate = parseCreditQueryDate(query.getQueryDate());
            if (queryDate == null || queryDate.isBefore(cutoff)) {
                continue;
            }
            if (reasonWhitelist != null && !reasonWhitelist.contains(query.getQueryReason())) {
                continue;
            }
            count++;
        }
        return count;
    }

    private LocalDate resolveCreditBaseDate(PersonalCreditReportRaw report) {
        if (report.getReportTime() != null && !report.getReportTime().isBlank()) {
            try {
                return LocalDateTime.parse(report.getReportTime(), CREDIT_REPORT_TIME).toLocalDate();
            } catch (DateTimeParseException ignored) {
                // ignore and fallback
            }
        }
        return LocalDate.now();
    }

    private LocalDate parseCreditQueryDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), CREDIT_QUERY_DATE);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private Integer parseAgeFromIdNo(String idNo) {
        if (idNo == null || idNo.length() < 14) {
            return null;
        }
        String birthText = idNo.substring(6, 14);
        try {
            LocalDate birthDate = LocalDate.parse(birthText, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private LocalDate parseLocalDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.trim();
        int blankIdx = normalized.indexOf(' ');
        if (blankIdx > 0) {
            normalized = normalized.substring(0, blankIdx);
        }
        try {
            return LocalDate.parse(normalized, ENTERPRISE_DATE);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String extractRegion(JsonNode areaNode) {
        if (areaNode == null || areaNode.isNull() || areaNode.isMissingNode()) {
            return null;
        }
        String province = trimToNull(areaNode.path("Province").asText(null));
        String city = trimToNull(areaNode.path("City").asText(null));
        if (province == null && city == null) {
            return null;
        }
        if (province == null) {
            return city;
        }
        if (city == null || province.equals(city)) {
            return province;
        }
        return province + city;
    }

    private String extractRegion(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        String value = address.trim();
        int provinceIdx = value.indexOf('省');
        if (provinceIdx > 0) {
            return value.substring(0, provinceIdx + 1);
        }
        int cityIdx = value.indexOf('市');
        if (cityIdx > 0) {
            return value.substring(0, cityIdx + 1);
        }
        return value.length() <= 6 ? value : value.substring(0, 6);
    }

    private BigDecimal extractLegalRepShareRatio(JsonNode partners, String legalRepName) {
        if (partners == null || !partners.isArray() || legalRepName == null || legalRepName.isBlank()) {
            return null;
        }
        for (JsonNode partner : partners) {
            String stockName = trimToNull(partner.path("StockName").asText(null));
            if (!legalRepName.equals(stockName)) {
                continue;
            }
            BigDecimal directShareRatio = percentageStringOrNull(partner.path("StockPercent").asText(null));
            if (directShareRatio != null) {
                return directShareRatio;
            }
        }
        return null;
    }

    private ChangeStats extractLegalRepChangeStats(JsonNode changeRecords) {
        if (changeRecords == null || !changeRecords.isArray()) {
            return ChangeStats.empty();
        }
        LocalDate now = LocalDate.now();
        int count2y = 0;
        LocalDate latestChangeDate = null;
        for (JsonNode changeRecord : changeRecords) {
            String projectName = changeRecord.path("ProjectName").asText(null);
            if (!isLegalRepChangeProject(projectName)) {
                continue;
            }
            LocalDate changeDate = parseLocalDate(changeRecord.path("ChangeDate").asText(null));
            if (changeDate == null) {
                continue;
            }
            if (!changeDate.isBefore(now.minusYears(2))) {
                count2y++;
            }
            if (latestChangeDate == null || changeDate.isAfter(latestChangeDate)) {
                latestChangeDate = changeDate;
            }
        }

        Integer lastGapMonths = null;
        if (latestChangeDate != null) {
            Period period = Period.between(latestChangeDate, now);
            lastGapMonths = period.getYears() * 12 + period.getMonths();
        }
        return new ChangeStats(count2y == 0 && latestChangeDate == null ? null : count2y, lastGapMonths);
    }

    private boolean isLegalRepChangeProject(String projectName) {
        return containsAny(projectName, "法定代表人", "负责人", "经营者", "执行事务合伙人");
    }

    private TagExtraction extractEnterpriseTags(JsonNode tagList) {
        if (tagList == null || !tagList.isArray()) {
            return TagExtraction.empty();
        }
        List<String> names = new ArrayList<>();
        Set<Integer> codes = new LinkedHashSet<>();
        for (JsonNode tagNode : tagList) {
            String tagName = trimToNull(tagNode.path("Name").asText(null));
            if (tagName == null) {
                continue;
            }
            Integer mappedCode = mapEnterpriseTagCode(tagName);
            if (mappedCode != null) {
                names.add(tagName);
                codes.add(mappedCode);
            }
        }
        return new TagExtraction(names, codes);
    }

    private Integer mapEnterpriseTagCode(String tagName) {
        if (tagName == null) {
            return null;
        }
        if (containsAny(tagName, "高新技术企业")) {
            return 3;
        }
        if (containsAny(tagName, "科技型中小企业", "创新型中小企业")) {
            return 2;
        }
        if (containsAny(tagName, "专精特新小巨人", "瞪羚")) {
            return 5;
        }
        if (containsAny(tagName, "专精特新")) {
            return 4;
        }
        if (containsAny(tagName, "小微企业", "小型微利")) {
            return 1;
        }
        return null;
    }

    private BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal percentageStringOrNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.replace("%", "").trim();
        return decimalOrNull(normalized);
    }

    private BigDecimal decimalOrNull(String text) {
        if (text == null || text.isBlank() || "--".equals(text)) {
            return null;
        }
        try {
            String normalized = text.replace(",", "").replace("万元", "").replace("万", "").trim();
            return new BigDecimal(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private BigDecimal decimalOrZero(String text) {
        BigDecimal value = decimalOrNull(text);
        return value == null ? BigDecimal.ZERO : value;
    }

    private Integer integerOrNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isActiveLoan(PersonalCreditReportRaw.LoanAccount loan) {
        return loan != null && (loan.getSettledMonth() == null || loan.getSettledMonth().isBlank());
    }

    private boolean isMortgageLoan(PersonalCreditReportRaw.LoanAccount loan) {
        return containsAny(loan.getLoanType(), "抵押", "按揭") || containsAny(loan.getAccountMode(), "抵押", "按揭");
    }

    private boolean hasAbnormalCreditAccount(PersonalCreditReportRaw report) {
        return report.getCreditCards().stream()
                .anyMatch(card -> containsAny(card.getAccountStatus(), "异常", "呆账", "止付", "冻结"))
                || report.getLoans().stream()
                .anyMatch(loan -> containsAny(loan.getCurrentStatus(), "异常", "呆账", "止付", "冻结"));
    }

    private boolean isNormalCreditClass(String value) {
        return value == null || value.isBlank() || "正常".equals(value) || "NORMAL".equalsIgnoreCase(value);
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private boolean containsAny(String value, String... keywords) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && keyword != null && value.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private void setIfHasText(JsonNode node, java.util.function.Consumer<String> setter) {
        String value = trimToNull(node == null ? null : node.asText(null));
        if (value != null) {
            setter.accept(value);
        }
    }

    private void addIfHasText(List<String> target, String value) {
        String normalized = trimToNull(value);
        if (normalized != null && !target.contains(normalized)) {
            target.add(normalized);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private int size(List<?> items) {
        return items == null ? 0 : items.size();
    }

    private int size(JsonNode node) {
        return node != null && node.isArray() ? node.size() : 0;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    @FunctionalInterface
    private interface DecimalSetter {
        void accept(BigDecimal value);
    }

    private record YearAggregate(BigDecimal amount, List<BigDecimal> monthlyAmounts, List<Integer> positiveMonths) {
        int positiveMonthCount() {
            return positiveMonths == null ? 0 : positiveMonths.size();
        }
    }

    private record ChangeStats(Integer count2y, Integer lastGapMonths) {
        private static ChangeStats empty() {
            return new ChangeStats(null, null);
        }
    }

    private record TagExtraction(List<String> names, Set<Integer> codes) {
        private static TagExtraction empty() {
            return new TagExtraction(List.of(), Set.of());
        }
    }
}
