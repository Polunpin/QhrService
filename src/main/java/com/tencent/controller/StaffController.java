package com.tencent.controller;

import com.tencent.config.*;
import com.tencent.dto.UpdateStatusRequest;
import com.tencent.model.Staff;
import com.tencent.service.StaffService;
import com.tencent.vo.Staffs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path("/api/staffs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StaffController {

  private final StaffService staffService;
  public StaffController(StaffService staffService) {
    this.staffService = staffService;
  }

  /** 查询员工详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    Staff staff = staffService.getById(id);
    ApiAssert.notNull(staff, ApiCode.NOT_FOUND, "员工不存在");
    return ApiResponse.ok(staff);
  }

  /** 分页查询员工列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("role") String role,
                          @QueryParam("status") Integer status,
                          @QueryParam("department") String department,
                          @QueryParam("mobile") String mobile,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<Staffs> staffs = staffService.list(role, status, department, mobile,
        bounds.offset(), bounds.size());
    long total = staffService.count(role, status, department, mobile);
    return ApiResponse.ok(PageResult.of(staffs, total, bounds.page(), bounds.size()));
  }

  /** 创建员工 */
  @POST
  public ApiResponse create(Staff staff) {
    Long id = staffService.create(staff);
    return ApiResponse.ok(id);
  }

  /** 更新员工 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, Staff staff) {
    ApiAssert.isTrue(staffService.update(staff.withId(id)), ApiCode.NOT_FOUND, "员工不存在");
    return ApiResponse.ok(true);
  }

  /** 删除员工 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(staffService.delete(id), ApiCode.NOT_FOUND, "员工不存在");
    return ApiResponse.ok(true);
  }

  /** 更新员工状态 */
  @POST
  @Path("/{id}/status")
  public ApiResponse updateStatus(@PathParam("id") Long id, UpdateStatusRequest request) {
    ApiAssert.isTrue(staffService.updateStatus(id, request.status()), ApiCode.NOT_FOUND, "员工不存在");
    return ApiResponse.ok(true);
  }
}
