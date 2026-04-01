package com.qhr.controller.jichu;

import com.qhr.config.*;
import com.qhr.model.User;
import com.qhr.service.UserService;
import com.qhr.vo.Users;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

/*用户管理*/
@Path("/api/users")
public class UserController {

  private final UserService userService;
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * 分页查询用户列表
   */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("page") Integer page, @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    PageResult<Users> list = userService.list(bounds.page(), bounds.size());
    return ApiResponse.ok(list);
  }

  /** 创建用户 */
  @POST
  public ApiResponse create(@Context HttpHeaders headers) {
    String openid = headers.getHeaderString("x-wx-openid");
    String unionid = headers.getHeaderString("x-wx-unionid");
    return ApiResponse.ok(userService.create(openid, unionid));
  }

  /** 更新用户 */
  @PUT
  public ApiResponse update(User user) {
    ApiAssert.notNull(user, ApiCode.BAD_REQUEST, "请求体user不能为空");
    return ApiResponse.ok(userService.update(user));
  }

  /** 删除用户 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(userService.delete(id));
  }

  /** 查询用户详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(userService.getById(id));
  }
}
