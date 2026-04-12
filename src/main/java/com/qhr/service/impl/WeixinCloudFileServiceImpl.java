package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.service.WeixinCloudFileService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 微信云托管文件服务实现。
 * 先调微信接口换取临时下载地址，再下载 PDF 原始字节。
 */
@ApplicationScoped
public class WeixinCloudFileServiceImpl implements WeixinCloudFileService {

    private static final Logger LOG = Logger.getLogger(WeixinCloudFileServiceImpl.class);

    //下载url
    private static final String BATCH_DOWNLOAD_PATH = "https://api.weixin.qq.com/tcb/batchdownloadfile";
    //删除url
    private static final String BATCH_DELETE_PATH = "https://api.weixin.qq.com/tcb/batchdeletefile";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    @ConfigProperty(name = "wechat.cloud.env")
    String env;

    public WeixinCloudFileServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * 批量下载接口一次只传一个 fileId，拿到 download_url 后再取文件内容。
     */
    @Override
    public byte[] download(String fileId) {
        JsonNode response = postJson(URI.create(BATCH_DOWNLOAD_PATH), Map.of(
                "env", env,
                "file_list", List.of(Map.of(
                        "fileid", fileId,
                        "max_age", 7200
                ))
        ), "调用微信云托管批量下载文件接口");

        JsonNode fileItem = response.path("file_list").path(0);
        int status = fileItem.path("status").asInt(Integer.MIN_VALUE);
        if (status != 0) {
            throw new ApiException(ApiCode.BAD_REQUEST, "获取云文件下载地址失败: " + fileItem.path("errmsg").asText("未知错误"));
        }
        String downloadUrl = fileItem.path("download_url").asText();
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new ApiException(ApiCode.BAD_REQUEST, "云文件下载地址为空");
        }
        return getBytes(downloadUrl, "下载微信云托管文件");
    }

    /**
     * 解析完成后删除云端原文件，避免临时征信文件长期滞留。
     */
    @Override
    public void delete(String fileId) {
        JsonNode response = postJson(URI.create(BATCH_DELETE_PATH), Map.of(
                "env", env,
                "fileid_list", List.of(fileId)
        ), "调用微信云托管删除文件接口");
        int errCode = response.path("errcode").asInt(0);
        if (errCode != 0) {
            throw new ApiException(ApiCode.BAD_REQUEST, "删除云文件失败: " + response.path("errmsg").asText("未知错误"));
        }
    }

    /**
     * 向微信云接口发送 JSON 请求，并统一校验 errcode。
     */
    private JsonNode postJson(URI uri, Object requestBody, String action) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .build();
        } catch (IOException exception) {
            throw new ApiException(ApiCode.INTERNAL_ERROR, action + "请求序列化失败: " + exception.getMessage());
        }

        HttpResponse<String> response = sendForText(request, action);
        try {
            JsonNode root = objectMapper.readTree(response.body());
            int errCode = root.path("errcode").asInt(0);
            if (errCode != 0) {
                throw new ApiException(ApiCode.BAD_REQUEST, action + "失败: " + root.path("errmsg").asText("未知错误"));
            }
            return root;
        } catch (IOException exception) {
            throw new ApiException(ApiCode.BAD_REQUEST, action + "响应解析失败: " + exception.getMessage());
        }
    }

    /**
     * 真正下载文件字节，供 PDF 解析器直接消费。
     */
    private byte[] getBytes(String url, String action) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new ApiException(ApiCode.BAD_REQUEST, action + "失败，HTTP状态码=" + response.statusCode());
            }

            return response.body();
        } catch (IOException exception) {
            throw new ApiException(ApiCode.BAD_REQUEST, action + "失败: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ApiCode.INTERNAL_ERROR, action + "被中断");
        }
    }

    /**
     * 统一封装文本 HTTP 调用，避免下载/删除接口重复写错误处理。
     */
    private HttpResponse<String> sendForText(HttpRequest request, String action) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new ApiException(ApiCode.BAD_REQUEST, action + "失败，HTTP状态码=" + response.statusCode());
            }
            return response;
        } catch (IOException exception) {
            throw new ApiException(ApiCode.BAD_REQUEST, action + "失败: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ApiCode.INTERNAL_ERROR, action + "被中断");
        }
    }

    /**
     * PDF 文件头不一定恰好位于第 0 字节，部分下载链路可能带前置空白或 BOM。
     */
    private boolean looksLikePdf(byte[] bytes) {
        if (bytes == null || bytes.length < 5) {
            return false;
        }
        byte[] marker = "%PDF-".getBytes(StandardCharsets.US_ASCII);
        int searchLimit = Math.min(bytes.length - marker.length + 1, 1024);
        for (int i = 0; i < searchLimit; i++) {
            boolean matched = true;
            for (int j = 0; j < marker.length; j++) {
                if (bytes[i + j] != marker[j]) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return true;
            }
        }
        return false;
    }

    /**
     * 二进制内容前缀转成可读字符串，便于快速定位微信侧返回的是 JSON、HTML 还是其他文本。
     */
    private String abbreviateBinary(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        int previewLength = Math.min(bytes.length, 200);
        String preview = new String(bytes, 0, previewLength, StandardCharsets.UTF_8)
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        if (preview.length() <= 160) {
            return preview;
        }
        return preview.substring(0, 160) + "...";
    }

}
