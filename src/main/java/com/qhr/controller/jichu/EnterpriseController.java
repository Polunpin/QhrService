package com.qhr.controller.jichu;

import com.qhr.config.*;
import com.qhr.model.Enterprise;
import com.qhr.service.EnterpriseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/*企业管理*/
@ApplicationScoped
@Path("/api/enterprises")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnterpriseController {

  private final EnterpriseService enterpriseService;
  public EnterpriseController(EnterpriseService enterpriseService) {
    this.enterpriseService = enterpriseService;
  }

  /** 分页查询企业列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    PageResult<Enterprise> enterprises = enterpriseService.list(bounds.offset(), bounds.size());
    return ApiResponse.ok(enterprises);
  }

  /** 分页查询用户关联企业 */
  @GET
  @Path("/user/{userId}")
  public ApiResponse listByUser(@PathParam("userId") Long userId,
                                @QueryParam("page") Integer page,
                                @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    PageResult<Enterprise> enterprises = enterpriseService.listByUserId(userId, bounds.offset(), bounds.size());
    return ApiResponse.ok(enterprises);
  }

  /** 创建企业 */
  @POST
  public ApiResponse create(Enterprise enterprise) {
    ApiAssert.notNull(enterprise, ApiCode.BAD_REQUEST, "请求体enterprise不能为空");
    return ApiResponse.ok(enterpriseService.create(enterprise));
  }

  /** 更新企业 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(Enterprise enterprise) {
    ApiAssert.notNull(enterprise, ApiCode.BAD_REQUEST, "请求体enterprise不能为空");
    return ApiResponse.ok(enterpriseService.update(enterprise));
  }

  /** 删除企业 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(enterpriseService.delete(id));
  }

  /**
   * 查询企业详情
   */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(enterpriseService.getById(id));
  }

}
