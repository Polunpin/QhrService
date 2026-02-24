package com.tencent.controller;

import com.tencent.config.ApiResponse;
import com.tencent.dto.BindEnterpriseRequest;
import com.tencent.dto.UpdateStatusRequest;
import com.tencent.dto.UsersRequest;
import com.tencent.model.Enterprise;
import com.tencent.model.User;
import com.tencent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.tencent.config.ApiAssert;
import com.tencent.config.ApiCode;
import com.tencent.config.PageBounds;
import com.tencent.config.PageResult;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(@Autowired UserService userService) {
    this.userService = userService;
  }

  /** 查询用户详情 */
  @GetMapping("/{id}")
  public ApiResponse getById(@PathVariable Long id) {
    User user = userService.getById(id);
    ApiAssert.notNull(user, ApiCode.NOT_FOUND, "用户不存在");
    return ApiResponse.ok(user);
  }

  /** 分页查询用户列表 */
  @GetMapping("/list")
  public Object list(@RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String mobile,
                          @RequestParam(required = false) String realName,
                          @RequestParam(required = false) Integer page,
                          @RequestParam(required = false) Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<UsersRequest> users = userService.list(status, mobile, realName, bounds.offset(), bounds.size());
    long total = userService.count(status, mobile, realName);
    return ApiResponse.ok(PageResult.of(users, total, bounds.page(), bounds.size()));
  }

  /** 创建用户 */
  @PostMapping
  public ApiResponse create(@RequestBody User user) {
    Long id = userService.create(user);
    return ApiResponse.ok(id);
  }

  /** 更新用户 */
  @PutMapping("/{id}")
  public ApiResponse update(@PathVariable Long id, @RequestBody User user) {
    ApiAssert.isTrue(userService.update(user.withId(id)), ApiCode.NOT_FOUND, "用户不存在");
    return ApiResponse.ok(true);
  }

  /** 删除用户 */
  @DeleteMapping("/{id}")
  public ApiResponse delete(@PathVariable Long id) {
    ApiAssert.isTrue(userService.delete(id), ApiCode.NOT_FOUND, "用户不存在");
    return ApiResponse.ok(true);
  }

  /** 更新用户状态 */
  @PostMapping("/{id}/status")
  public ApiResponse updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
    ApiAssert.isTrue(userService.updateStatus(id, request.status()), ApiCode.NOT_FOUND, "用户不存在");
    return ApiResponse.ok(true);
  }

  /** 绑定用户与企业关系 */
  @PostMapping("/{id}/enterprises/{enterpriseId}")
  public ApiResponse bindEnterprise(@PathVariable Long id,
                                    @PathVariable Long enterpriseId,
                                    @RequestBody(required = false) BindEnterpriseRequest request) {
    String role = request == null ? null : request.role();
    return ApiResponse.ok(userService.bindEnterprise(id, enterpriseId, role));
  }

  /** 解绑用户与企业关系 */
  @DeleteMapping("/{id}/enterprises/{enterpriseId}")
  public ApiResponse unbindEnterprise(@PathVariable Long id,
                                      @PathVariable Long enterpriseId) {
    return ApiResponse.ok(userService.unbindEnterprise(id, enterpriseId));
  }

  /** 分页查询用户关联企业 */
  @GetMapping("/{id}/enterprises")
  public ApiResponse listEnterprises(@PathVariable Long id,
                                     @RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<Enterprise> enterprises = userService.listEnterprises(id, bounds.offset(), bounds.size());
    long total = userService.countEnterprises(id);
    return ApiResponse.ok(PageResult.of(enterprises, total, bounds.page(), bounds.size()));
  }

  /** 分页查询企业关联用户 */
  @GetMapping("/enterprise/{enterpriseId}/users")
  public ApiResponse listUsersByEnterprise(@PathVariable Long enterpriseId,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<User> users = userService.listUsersByEnterprise(enterpriseId, bounds.offset(), bounds.size());
    long total = userService.countUsersByEnterprise(enterpriseId);
    return ApiResponse.ok(PageResult.of(users, total, bounds.page(), bounds.size()));
  }
}
