package com.qhr.service;

/**
 * 微信云托管文件读写能力。
 */
public interface WeixinCloudFileService {

    /**
     * 通过 fileId 下载云文件内容。
     */
    byte[] download(String fileId);

    /**
     * 解析结束后删除云端临时文件，避免重复占用存储。
     */
    void delete(String fileId);
}
