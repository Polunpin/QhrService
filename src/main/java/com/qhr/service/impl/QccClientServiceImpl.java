package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.qcc.QccAuthSigner;
import com.qhr.config.qcc.QccClientException;
import com.qhr.dto.QccTaxCreateOrderRequest;
import com.qhr.service.QccClientService;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ApplicationScoped
public class QccClientServiceImpl implements QccClientService {

    @ConfigProperty(name = "qcc-key")
    String appKey;
    @ConfigProperty(name = "qcc-secretKey")
    String secretKey;

    //企业模糊查询
    private static final String FUZZY_SEARCH_URL = "https://api.qichacha.com/FuzzySearch/GetList";
    //企业财税数据 第一步：创建订单
    private static final String TAX_CREATE_ORDER_URL = "https://api.qichacha.com/TaxData/CreateOrder";
    //企业财税数据 第二步：验证码发送
    private static final String TAX_SEND_CODE_URL = "https://api.qichacha.com/TaxData/SendCode";
    //企业财税数据 第三步：数据获取
    private static final String TAX_GET_DATA_URL = "https://api.qichacha.com/TaxData/GetData";
    //企业工商详情
    private static final String GET_INFO_URL = "https://api.qichacha.com/ECIInfoVerify/GetInfo";

    private final QccAuthSigner qccAuthSigner;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public QccClientServiceImpl(QccAuthSigner qccAuthSigner, ObjectMapper objectMapper) {
        this.qccAuthSigner = qccAuthSigner;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @SneakyThrows
    @Override
    public JsonNode getList(String searchKey) {

//        QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
//        HttpRequest request = HttpRequest.newBuilder(buildUri(
//                        FUZZY_SEARCH_URL,
//                        "key", appKey,
//                        "searchKey", searchKey
//                ))
//                .GET()
//                .timeout(Duration.ofSeconds(10))
//                .header("Token", authHeaders.token())
//                .header("Timespan", authHeaders.timespan())
//                .build();
//        return execute(request, "调用企查查企业模糊搜索接口");

        //TODO-数据模拟测试
        String str1 = "{\"KeyNo\":\"0si5hyjhbliespkjb9vi5ias5askla5jl5\",\"Name\":\"北京护安行教育科技有限公司\",\"CreditCode\":\"91110108MADH4BC12G\",\"StartDate\":\"2024-04-02\",\"OperName\":\"兰一平\",\"Status\":\"存续\",\"No\":\"110108041266562\",\"Address\":\"北京市海淀区白家疃尚峰园2号楼3层302\"}";
        return new ObjectMapper().readTree(str1);

    }


    @SneakyThrows
    @Override
    public JsonNode createTaxOrder(QccTaxCreateOrderRequest requestBody) {

//    QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
//    HttpRequest request = HttpRequest.newBuilder(buildUri(
//                    TAX_CREATE_ORDER_URL,
//                    "key", appKey
//            ))
//            .POST(buildJsonBody(requestBody))
//            .timeout(Duration.ofSeconds(10))
//            .header("Token", authHeaders.token())
//            .header("Timespan", authHeaders.timespan())
//            .header("Content-Type", "application/json")
//            .build();
//    return execute(request, "调用企查查财税下单接口");

        //TODO-数据模拟测试
        String str1 = "{\"Status\":\"200\",\"Message\":\"【有效请求】查询成功\",\"OrderNumber\":\"TAXDATA2024052815351837763994\",\"Result\":{\"DataStatus\":\"S\",\"OrderNo\":\"2697445xxxx278784\",\"OrderResult\":\"认证成功！\"}}";
        return new ObjectMapper().readTree(str1).path("Result");

    }

    @SneakyThrows
    @Override
    public JsonNode sendCode(String orderNo, String verifyCode) {

//    QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
//    HttpRequest request = HttpRequest.newBuilder(buildUri(
//                    TAX_SEND_CODE_URL,
//                    "key", appKey,
//                    "orderNo", orderNo,
//                    "verifyCode", verifyCode
//            ))
//            .GET()
//            .timeout(Duration.ofSeconds(10))
//            .header("Token", authHeaders.token())
//            .header("Timespan", authHeaders.timespan())
//            .build();
//    return execute(request, "调用企查查财税验证码发送接口");

        //TODO-数据模拟测试
        String str1 = "{\"Status\":\"200\",\"Message\":\"【有效请求】查询成功\",\"OrderNumber\":\"TAXDATA2024052815351837763921\",\"Result\":{\"DataStatus\":\"S\",\"OrderNo\":\"2697445331xxxx784\",\"OrderResult\":\"认证成功！\"}}";
        return new ObjectMapper().readTree(str1).path("Result");

    }


    @SneakyThrows
    @Override
    public JsonNode getTaxData(String orderNo) {

//        QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
//        HttpRequest request = HttpRequest.newBuilder(buildUri(
//                        TAX_GET_DATA_URL,
//                        "key", appKey,
//                        "orderNo", orderNo
//                ))
//                .GET()
//                .timeout(Duration.ofSeconds(10))
//                .header("Token", authHeaders.token())
//                .header("Timespan", authHeaders.timespan())
//                .build();
//        return execute(request, "调用企查查财税数据获取接口");

        //TODO-数据模拟测试
        String str1 = "{\"Status\":\"200\",\"Message\":\"【有效请求】查询成功\",\"OrderNumber\":\"TAXDATA2024052814530913996681\",\"Result\":{\"DataStatus\":\"S\",\"Data\":{\"FinancialIndexList\":[{\"IndexName\":\"毛利率\",\"ValueList\":[{\"Date\":\"2024-03-31\",\"Value\":\"14.00%\"},{\"Date\":\"2023\",\"Value\":\"12.00%\"},{\"Date\":\"2022\",\"Value\":\"17.00%\"}]}],\"DeclarationDetail\":{\"CorporateInTaxDeclareList\":[{\"ThisYearSaleRevenue\":\"49.84\",\"ThisYearCumulativeProfit\":\"-0.82\",\"StartDate\":\"2024-01-01\",\"EndDate\":\"2024-03-31\",\"TaxPayable\":\"0.00\",\"WithholdingTax\":\"0.00\",\"TaxCompensate\":\"0.00\",\"TaxDeduction\":\"0.00\"}],\"ValueAddedTaxDeclareList\":[{\"AllSaleRevenue\":\"21.52\",\"AllCumulativeRevenue\":\"21.52\",\"StartDate\":\"2024-04-01\",\"EndDate\":\"2024-04-30\",\"TaxPayable\":\"0.11\",\"WithholdingTax\":\"0.00\",\"TaxCompensate\":\"0.11\",\"TaxDeduction\":\"0.00\"}],\"OtherTaxDeclareList\":[{\"LevyItemCode\":\"STAMP_TAX\",\"LevyItemValue\":\"印花税\",\"TaxBasis\":\"0.76\",\"StartDate\":\"2025-07-01\",\"EndDate\":\"2025-09-30\",\"TaxPayable\":\"0.00\",\"WithholdingTax\":\"0.00\",\"TaxCompensate\":\"0.00\",\"TaxDeduction\":\"0.00\"}]},\"CollectionDetail\":{\"CorporateInTaxCollectionList\":[{\"ThisYearCumulativeProfit\":\"\",\"StartDate\":\"2023-04-01\",\"EndDate\":\"2023-06-30\",\"PaymentLimitDate\":\"2023-07-17\",\"PaymentDate\":\"2023-07-14\",\"TaxType\":\"正税\",\"TaxRate\":\"\",\"ActualAmount\":\"0.03\"}],\"ValueAddedTaxCollectionList\":[{\"SaleRevenue\":\"\",\"StartDate\":\"2024-04-01\",\"EndDate\":\"2024-04-30\",\"PaymentLimitDate\":\"2024-05-22\",\"PaymentDate\":\"2024-05-17\",\"TaxType\":\"正税\",\"TaxRate\":\"\",\"ActualAmount\":\"0.11\"}],\"OtherTaxCollectionList\":[{\"LevyItemCode\":\"LOCAL_EDUCATION_SURCHARGE\",\"LevyItemValue\":\"地方教育附加\",\"TaxBasis\":\"\",\"StartDate\":\"2024-04-01\",\"EndDate\":\"2024-04-30\",\"PaymentLimitDate\":\"2024-05-22\",\"PaymentDate\":\"2024-05-17\",\"TaxType\":\"正税\",\"TaxRate\":\"\",\"ActualAmount\":\"0\"}]},\"SaleList\":[{\"Year\":\"2024\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"44.28\"}]}],\"TaxData\":{\"TotalTaxList\":[{\"Year\":\"2024\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"0.18\"}]}],\"CorporateInTaxList\":[{\"Year\":\"2024\",\"AnnualTax\":\"\",\"QuarterlyDataList\":[{\"Quarter\":\"1\",\"Amount\":\"0.00\"}]}],\"ValueAddedTaxList\":[{\"Year\":\"2024\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"0.18\"}]}],\"OtherTaxList\":[{\"LevyItemCode\":\"STAMP_TAX\",\"LevyItemValue\":\"印花税\",\"YearDataList\":[{\"Year\":\"2025\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"0.83\"}]}]}]},\"TaxBurdenRateList\":[{\"LevyItemCode\":\"VAT\",\"LevyItemValue\":\"增值税\",\"DataList\":[{\"Year\":\"2023\",\"Ratio\":\"1.03%\"},{\"Year\":\"2022\",\"Ratio\":\"1.02%\"},{\"Year\":\"2021\",\"Ratio\":\"1.00%\"}]}],\"FinancialList\":[{\"Type\":\"BALANCE_SHEET\",\"TypeValue\":\"资产负债表\",\"SubjectList\":[{\"Subject\":\"货币资金\",\"RevenueList\":[{\"Year\":\"2022\",\"Amount\":\"37.65\"},{\"Year\":\"2023\",\"Amount\":\"3.77\"},{\"Year\":\"2024-03-31\",\"Amount\":\"0.00\"}]}]}],\"SupplierCustomerList\":[{\"Type\":\"SUPPLIER\",\"TypeValue\":\"供应商\",\"YearDataList\":[{\"Year\":\"2023\",\"SupplierCustomerInfoList\":[{\"Name\":\"浙江联塑科技实业有限公司\",\"Amount\":\"36.10\",\"Proportion\":\"18.81%\"}]}]}],\"TopCustomerList\":[{\"Year\":\"2024\",\"RepeatCount\":\"3\",\"RepeatAmont\":\"23.24\",\"TotalAmount\":\"68.74\",\"PurchaseProportion\":\"94.74%\"}],\"TopSupplierList\":[{\"Year\":\"2024\",\"RepeatCount\":\"6\",\"TotalAmount\":\"69.04\",\"SupplierProportion\":\"96.13%\"}],\"BreakLawDetailList\":[],\"BreakLawSummaryList\":[],\"ExpenseDetail\":{\"ElectricityExpenseList\":[{\"Year\":\"2024\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"0.35\"}]}],\"WaterExpenseList\":[{\"Year\":\"2024\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"3.21\"}]}],\"GasExpenseList\":[{\"Year\":\"2024\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"0.01\"}]}],\"HouseRentalExpenseList\":[{\"Year\":\"2024\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"19.12\"}]}],\"TransportAndStorageExpenseList\":[{\"Year\":\"2024\",\"DataList\":[{\"Month\":\"1\",\"Amount\":\"3.76\"}]}]},\"CashFlowList\":[{\"Subject\":\"销售商品提供劳务收到的现金\",\"RevenueList\":[{\"Year\":2023,\"Amount\":\"140.20\"}]}]}}}";
        return new ObjectMapper().readTree(str1).path("Result");

    }

    @SneakyThrows
    @Override
    public JsonNode getInfo(String searchKey) {

//        QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
//        HttpRequest request = HttpRequest.newBuilder(buildUri(
//                        GET_INFO_URL,
//                        "key", appKey,
//                        "searchKey", searchKey
//                ))
//                .GET()
//                .timeout(Duration.ofSeconds(10))
//                .header("Token", authHeaders.token())
//                .header("Timespan", authHeaders.timespan())
//                .build();
//        return execute(request, "调用企查查-企业工商详情索 接口");

        //TODO-数据模拟测试
        String str1 = "{\"Status\":\"200\",\"Message\":\"【有效请求】查询成功\",\"OrderNumber\":\"ECI2021070315333295644981\",\"Result\":{\"Partners\":[{\"StockName\":\"兰一平\",\"StockType\":\"自然人股东\",\"StockPercent\":\"28.8334%\",\"FinalBenefitPercent\":\"39.9559%\",\"TagsList\":[\"大股东\",\"实际控制人\",\"最终受益人\"]},{\"StockName\":\"北京某投资有限公司\",\"StockType\":\"企业法人\",\"StockPercent\":\"18.0000%\",\"FinalBenefitPercent\":\"18.0989%\",\"TagsList\":[]}],\"ChangeRecords\":[{\"ProjectName\":\"法定代表人变更\",\"BeforeContent\":\"张三\",\"AfterContent\":\"兰一平\",\"ChangeDate\":\"2025-01-15\"},{\"ProjectName\":\"注册资本变更（注册资金、资金数额等变更）\",\"BeforeContent\":\"36000.000000万元\",\"AfterContent\":\"36225.000000万元（+225万元）\",\"ChangeDate\":\"2023-10-30\"}],\"Industry\":{\"IndustryCode\":\"M\",\"Industry\":\"科学研究和技术服务业\",\"SubIndustryCode\":\"73\",\"SubIndustry\":\"研究和试验发展\"},\"Area\":{\"Province\":\"北京市\",\"City\":\"北京市\",\"County\":\"海淀区\"},\"TagList\":[{\"Type\":\"903\",\"Name\":\"存续\"},{\"Type\":\"108\",\"Name\":\"高新技术企业\"},{\"Type\":\"91\",\"Name\":\"科技型中小企业\"}],\"Name\":\"北京护安行教育科技有限公司\",\"OperName\":\"兰一平\",\"StartDate\":\"2024-04-02 00:00:00\",\"Status\":\"存续（在营、开业、在册）\",\"CreditCode\":\"91110108MADH4BC12G\",\"RegisteredCapital\":\"5000\",\"PaidUpCapital\":\"5000\",\"Address\":\"北京市海淀区白家疃尚峰园2号楼3层302\"}}";
        return new ObjectMapper().readTree(str1).path("Result");

    }

    private URI buildUri(String baseUrl, String... queryPairs) {
        if (queryPairs.length % 2 != 0) {
            throw new IllegalArgumentException("queryPairs必须是成对的key/value");
        }
        StringBuilder builder = new StringBuilder(baseUrl);
        boolean hasQuery = false;
        for (int i = 0; i < queryPairs.length; i += 2) {
            String key = queryPairs[i];
            String value = queryPairs[i + 1];
            if (value == null || value.isBlank()) {
                continue;
            }
            builder.append(hasQuery ? '&' : '?')
                    .append(encode(key))
                    .append('=')
                    .append(encode(value));
            hasQuery = true;
        }
        return URI.create(builder.toString());
    }

    private HttpRequest.BodyPublisher buildJsonBody(Object requestBody) {
        try {
            return HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new QccClientException("序列化企查查请求失败", exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


    private JsonNode execute(HttpRequest request, String action) {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new QccClientException(action + "失败", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new QccClientException(action + "被中断", exception);
        }

        if (response.statusCode() != 200) {
            throw new QccClientException(action + "失败，HTTP状态码=" + response.statusCode());
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("Result");
        } catch (IOException exception) {
            throw new QccClientException(action + "响应解析失败", exception);
        }
    }

}
