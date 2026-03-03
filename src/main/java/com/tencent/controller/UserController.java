package com.tencent.controller;

import com.tencent.config.*;
import com.tencent.dto.BindEnterpriseRequest;
import com.tencent.dto.UpdateStatusRequest;
import com.tencent.model.Enterprise;
import com.tencent.model.User;
import com.tencent.service.UserService;
import com.tencent.vo.Users;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

  private final UserService userService;
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /** 查询用户详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    User user = userService.getById(id);
    ApiAssert.notNull(user, ApiCode.NOT_FOUND, "用户不存在");
    return ApiResponse.ok(user);
  }

  /** 分页查询用户列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("status") Integer status,
                          @QueryParam("mobile") String mobile,
                          @QueryParam("realName") String realName,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<Users> users = userService.list(status, mobile, realName, bounds.offset(), bounds.size());
    long total = userService.count(status, mobile, realName);
    return ApiResponse.ok(PageResult.of(users, total, bounds.page(), bounds.size()));
  }

  /** 创建用户 */
  @POST
  public ApiResponse create(User user) {
    Long id = userService.create(user);
    return ApiResponse.ok(id);
  }

  /** 更新用户 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, User user) {
    ApiAssert.isTrue(userService.update(user.withId(id)), ApiCode.NOT_FOUND, "用户不存在");
    return ApiResponse.ok(true);
  }

  /** 删除用户 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(userService.delete(id), ApiCode.NOT_FOUND, "用户不存在");
    return ApiResponse.ok(true);
  }

  /** 更新用户状态 */
  @POST
  @Path("/{id}/status")
  public ApiResponse updateStatus(@PathParam("id") Long id, UpdateStatusRequest request) {
    ApiAssert.isTrue(userService.updateStatus(id, request.status()), ApiCode.NOT_FOUND, "用户不存在");
    return ApiResponse.ok(true);
  }

  /** 绑定用户与企业关系 */
  @POST
  @Path("/{id}/enterprises/{enterpriseId}")
  public ApiResponse bindEnterprise(@PathParam("id") Long id,
                                    @PathParam("enterpriseId") Long enterpriseId,
                                    BindEnterpriseRequest request) {
    String role = request == null ? null : request.role();
    return ApiResponse.ok(userService.bindEnterprise(id, enterpriseId, role));
  }

  /** 解绑用户与企业关系 */
  @DELETE
  @Path("/{id}/enterprises/{enterpriseId}")
  public ApiResponse unbindEnterprise(@PathParam("id") Long id,
                                      @PathParam("enterpriseId") Long enterpriseId) {
    return ApiResponse.ok(userService.unbindEnterprise(id, enterpriseId));
  }

  /** 分页查询用户关联企业 */
  @GET
  @Path("/{id}/enterprises")
  public ApiResponse listEnterprises(@PathParam("id") Long id,
                                     @QueryParam("page") Integer page,
                                     @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<Enterprise> enterprises = userService.listEnterprises(id, bounds.offset(), bounds.size());
    long total = userService.countEnterprises(id);
    return ApiResponse.ok(PageResult.of(enterprises, total, bounds.page(), bounds.size()));
  }

  /** 分页查询企业关联用户 */
  @GET
  @Path("/enterprise/{enterpriseId}/users")
  public ApiResponse listUsersByEnterprise(@PathParam("enterpriseId") Long enterpriseId,
                                           @QueryParam("page") Integer page,
                                           @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<User> users = userService.listUsersByEnterprise(enterpriseId, bounds.offset(), bounds.size());
    long total = userService.countUsersByEnterprise(enterpriseId);
    return ApiResponse.ok(PageResult.of(users, total, bounds.page(), bounds.size()));
  }
}
