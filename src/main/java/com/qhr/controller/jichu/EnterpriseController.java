package com.qhr.controller.jichu;

import com.qhr.config.*;
import com.qhr.model.Enterprise;
import com.qhr.service.EnterpriseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

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

  /** 查询企业详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    Enterprise enterprise = enterpriseService.getById(id);
    ApiAssert.notNull(enterprise, ApiCode.NOT_FOUND, "企业不存在");
    return ApiResponse.ok(enterprise);
  }

  /** 分页查询企业列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("name") String name,
                          @QueryParam("creditCode") String creditCode,
                          @QueryParam("operName") String operName,
                          @QueryParam("status") String status,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<Enterprise> enterprises = enterpriseService.list(name, creditCode, operName, status, bounds.offset(), bounds.size());
    long total = enterpriseService.count(name, creditCode, operName, status);
    return ApiResponse.ok(PageResult.of(enterprises, total, bounds.page(), bounds.size()));
  }

  /** 分页查询用户关联企业 */
  @GET
  @Path("/user/{userId}")
  public ApiResponse listByUser(@PathParam("userId") Long userId,
                                @QueryParam("page") Integer page,
                                @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<Enterprise> enterprises = enterpriseService.listByUserId(userId, bounds.offset(), bounds.size());
    long total = enterpriseService.countByUserId(userId);
    return ApiResponse.ok(PageResult.of(enterprises, total, bounds.page(), bounds.size()));
  }

  /** 创建企业 */
  @POST
  public ApiResponse create(Enterprise enterprise) {
    Long id = enterpriseService.create(enterprise);
    return ApiResponse.ok(id);
  }

  /** 更新企业 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, Enterprise enterprise) {
    ApiAssert.isTrue(enterpriseService.update(enterprise.withId(id)), ApiCode.NOT_FOUND, "企业不存在");
    return ApiResponse.ok(true);
  }

  /** 删除企业 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(enterpriseService.delete(id), ApiCode.NOT_FOUND, "企业不存在");
    return ApiResponse.ok(true);
  }
}
