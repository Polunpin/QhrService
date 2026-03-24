package com.qhr.config.qcc;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@ApplicationScoped
public class QccAuthSigner {

  public QccAuthHeaders sign(String appKey, String secretKey) {
    String timespan = Long.toString(Instant.now().getEpochSecond());
    return new QccAuthHeaders(md5Uppercase(appKey + timespan + secretKey), timespan);
  }

  private String md5Uppercase(String content) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      byte[] digest = messageDigest.digest(content.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(digest.length * 2);
      for (byte current : digest) {
        builder.append(String.format("%02X", current));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("JVM 不支持 MD5 算法", exception);
    }
  }
}
