package com.qhr.controller;

import com.qhr.config.*;
import com.qhr.dto.UpdateStringStatusRequest;
import com.qhr.model.MatchRecord;
import com.qhr.service.MatchRecordService;
import com.qhr.vo.MatchRecords;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path("/api/matching")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MatchRecordController {

  private final MatchRecordService matchRecordService;
  public MatchRecordController(MatchRecordService matchRecordService) {
    this.matchRecordService = matchRecordService;
  }

  /** 查询匹配记录详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    MatchRecord record = matchRecordService.getById(id);
    ApiAssert.notNull(record, ApiCode.NOT_FOUND, "匹配记录不存在");
    return ApiResponse.ok(record);
  }

  /** 分页查询匹配记录列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("enterpriseId") Long enterpriseId,
                          @QueryParam("intentionId") Long intentionId,
                          @QueryParam("status") String status,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<MatchRecords> records = matchRecordService.list(enterpriseId, intentionId, status,
        bounds.offset(), bounds.size());
    long total = matchRecordService.count(enterpriseId, intentionId, status);
    return ApiResponse.ok(PageResult.of(records, total, bounds.page(), bounds.size()));
  }

  /** 创建匹配记录 */
  @POST
  public ApiResponse create(MatchRecord record) {
    Long id = matchRecordService.create(record);
    return ApiResponse.ok(id);
  }

  /** 更新匹配记录 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, MatchRecord record) {
    ApiAssert.isTrue(matchRecordService.update(record.withId(id)), ApiCode.NOT_FOUND, "匹配记录不存在");
    return ApiResponse.ok(true);
  }

  /** 删除匹配记录 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(matchRecordService.delete(id), ApiCode.NOT_FOUND, "匹配记录不存在");
    return ApiResponse.ok(true);
  }

  /** 更新匹配记录状态 */
  @POST
  @Path("/{id}/status")
  public ApiResponse updateStatus(@PathParam("id") Long id,
                                  UpdateStringStatusRequest request) {
    ApiAssert.isTrue(matchRecordService.updateStatus(id, request.status()),
        ApiCode.NOT_FOUND, "匹配记录不存在");
    return ApiResponse.ok(true);
  }
}
