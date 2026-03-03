package com.tencent.controller;

import com.tencent.config.*;
import com.tencent.dto.UpdateMatchStatusRequest;
import com.tencent.dto.UpdateProfileDataRequest;
import com.tencent.model.Enterprise;
import com.tencent.service.EnterpriseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

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
  public ApiResponse list(@QueryParam("matchStatus") String matchStatus,
                          @QueryParam("industry") String industry,
                          @QueryParam("regionCode") String regionCode,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<Enterprise> enterprises = enterpriseService.list(matchStatus, industry, regionCode, bounds.offset(), bounds.size());
    long total = enterpriseService.count(matchStatus, industry, regionCode);
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

  /** 更新企业匹配状态 */
  @POST
  @Path("/{id}/match-status")
  public ApiResponse updateMatchStatus(@PathParam("id") Long id,
                                       UpdateMatchStatusRequest request) {
    ApiAssert.isTrue(enterpriseService.updateMatchStatus(id, request.matchStatus()),
        ApiCode.NOT_FOUND, "企业不存在");
    return ApiResponse.ok(true);
  }

  /** 更新企业画像数据 */
  @POST
  @Path("/{id}/profile-data")
  public ApiResponse updateProfileData(@PathParam("id") Long id,
                                       UpdateProfileDataRequest request) {
    ApiAssert.isTrue(enterpriseService.updateProfileData(id, request.profileData()),
        ApiCode.NOT_FOUND, "企业不存在");
    return ApiResponse.ok(true);
  }
}
