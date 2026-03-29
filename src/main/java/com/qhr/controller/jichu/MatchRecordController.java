package com.qhr.controller.jichu;

import com.qhr.config.*;
import com.qhr.model.MatchRecord;
import com.qhr.service.MatchRecordService;
import com.qhr.vo.MatchRecords;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/*匹配记录*/
@ApplicationScoped
@Path("/api/matching")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MatchRecordController {

  private final MatchRecordService matchRecordService;
  public MatchRecordController(MatchRecordService matchRecordService) {
    this.matchRecordService = matchRecordService;
  }

  /** 分页查询匹配记录列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("page") Integer page, @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    PageResult<MatchRecords> records = matchRecordService.list(bounds.offset(), bounds.size());
    return ApiResponse.ok(records);
  }

  /** 创建匹配记录 */
  @POST
  public ApiResponse create(MatchRecord record) {
    ApiAssert.notNull(record, ApiCode.BAD_REQUEST, "请求体record不能为空");
    return ApiResponse.ok(matchRecordService.create(record));
  }

  /** 更新匹配记录 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(MatchRecord record) {
    ApiAssert.notNull(record, ApiCode.BAD_REQUEST, "请求体record不能为空");
    return ApiResponse.ok(matchRecordService.update(record));
  }

  /** 删除匹配记录 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(matchRecordService.delete(id));
  }

  /**
   * 查询匹配记录详情
   */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(matchRecordService.getById(id));
  }
}
