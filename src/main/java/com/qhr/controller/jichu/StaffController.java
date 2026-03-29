package com.qhr.controller.jichu;

import com.qhr.config.*;
import com.qhr.model.Staff;
import com.qhr.service.StaffService;
import com.qhr.vo.Staffs;
import jakarta.ws.rs.*;

/*员工管理*/
@Path("/api/staffs")
public class StaffController {

  private final StaffService staffService;
  public StaffController(StaffService staffService) {
    this.staffService = staffService;
  }

  /** 分页查询员工列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    PageResult<Staffs> list = staffService.list(bounds.page(), bounds.size());
    return ApiResponse.ok(list);
  }

  /** 创建员工 */
  @POST
  public ApiResponse create(Staff staff) {
    ApiAssert.notNull(staff, ApiCode.BAD_REQUEST, "请求体staff不能为空");
    return ApiResponse.ok(staffService.create(staff));
  }

  /** 更新员工 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(Staff staff) {
    ApiAssert.notNull(staff, ApiCode.BAD_REQUEST, "请求体staff不能为空");
    return ApiResponse.ok(staffService.update(staff));
  }

  /** 删除员工 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(staffService.delete(id));
  }

  /**
   * 查询员工详情
   */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(staffService.getById(id));
  }
}
