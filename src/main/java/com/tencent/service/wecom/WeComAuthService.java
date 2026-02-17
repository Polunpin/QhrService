package com.tencent.service.wecom;

import com.tencent.config.ApiCode;
import com.tencent.config.ApiException;
import com.tencent.model.User;
import com.tencent.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class WeComAuthService {

  private final WeComClient wecomClient;
  private final UserService userService;

  public WeComAuthService(WeComClient wecomClient, UserService userService) {
    this.wecomClient = wecomClient;
    this.userService = userService;
  }

  public WecomLoginResponse loginByCode(String code) {
    if (code == null || code.isBlank()) {
      throw new ApiException(ApiCode.BAD_REQUEST, "code不能为空");
    }
    WeComClient.WeComAuthResponse authResponse = wecomClient.getAuthUser(code);
    if (authResponse.userid() == null || authResponse.userid().isBlank()) {
      throw new ApiException(ApiCode.BAD_REQUEST, "企业微信未返回用户标识");
    }
    WeComClient.WeComUserDetailResponse detail = wecomClient.getUserDetail(authResponse.userid());
    User existing = userService.getByOpenid(authResponse.userid());
    boolean created = false;
    if (existing == null) {
      User newUser = new User(null,
          authResponse.userid(),
          detail.unionid(),
          detail.mobile(),
          detail.name(),
          1,
          null,
          null);
      Long newId = userService.create(newUser);
      existing = userService.getById(newId);
      created = true;
    } else {
      User updated = mergeUser(existing, detail);
      if (!updated.equals(existing)) {
        userService.update(updated);
        existing = userService.getById(existing.id());
      }
    }
    return new WecomLoginResponse(existing.id(), authResponse.userid(), existing.realName(),
        existing.mobile(), created);
  }

  private User mergeUser(User existing, WeComClient.WeComUserDetailResponse detail) {
    String realName = detail.name() == null || detail.name().isBlank() ? existing.realName() : detail.name();
    String mobile = detail.mobile() == null || detail.mobile().isBlank() ? existing.mobile() : detail.mobile();
    String unionId = detail.unionid() == null || detail.unionid().isBlank() ? existing.unionid() : detail.unionid();
    if (realName.equals(existing.realName())
        && mobile.equals(existing.mobile())
        && unionId.equals(existing.unionid())) {
      return existing;
    }
    return new User(existing.id(), existing.openid(), unionId, mobile, realName, existing.status(),
        existing.createdAt(), existing.updatedAt());
  }

  public record WecomLoginResponse(Long userId,
                                   String weComUserId,
                                   String realName,
                                   String mobile,
                                   boolean isNew) {
  }
}
