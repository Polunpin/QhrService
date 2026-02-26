package com.tencent.controller;

import com.tencent.config.ApiResponse;
import com.tencent.model.Staff;
import com.tencent.vo.Staffs;
import com.tencent.dto.UpdateStatusRequest;
import com.tencent.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.tencent.config.ApiAssert;
import com.tencent.config.ApiCode;
import com.tencent.config.PageBounds;
import com.tencent.config.PageResult;

@RestController
@RequestMapping("/api/staffs")
public class StaffController {

  private final StaffService staffService;

  public StaffController(@Autowired StaffService staffService) {
    this.staffService = staffService;
  }

  /** 查询员工详情 */
  @GetMapping("/{id}")
  public ApiResponse getById(@PathVariable Long id) {
    Staff staff = staffService.getById(id);
    ApiAssert.notNull(staff, ApiCode.NOT_FOUND, "员工不存在");
    return ApiResponse.ok(staff);
  }

  /** 分页查询员工列表 */
  @GetMapping("/list")
  public ApiResponse list(@RequestParam(required = false) String role,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String department,
                          @RequestParam(required = false) String mobile,
                          @RequestParam(required = false) Integer page,
                          @RequestParam(required = false) Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<Staffs> staffs = staffService.list(role, status, department, mobile,
        bounds.offset(), bounds.size());
    long total = staffService.count(role, status, department, mobile);
    return ApiResponse.ok(PageResult.of(staffs, total, bounds.page(), bounds.size()));
  }

  /** 创建员工 */
  @PostMapping
  public ApiResponse create(@RequestBody Staff staff) {
    Long id = staffService.create(staff);
    return ApiResponse.ok(id);
  }

  /** 更新员工 */
  @PutMapping("/{id}")
  public ApiResponse update(@PathVariable Long id, @RequestBody Staff staff) {
    ApiAssert.isTrue(staffService.update(staff.withId(id)), ApiCode.NOT_FOUND, "员工不存在");
    return ApiResponse.ok(true);
  }

  /** 删除员工 */
  @DeleteMapping("/{id}")
  public ApiResponse delete(@PathVariable Long id) {
    ApiAssert.isTrue(staffService.delete(id), ApiCode.NOT_FOUND, "员工不存在");
    return ApiResponse.ok(true);
  }

  /** 更新员工状态 */
  @PostMapping("/{id}/status")
  public ApiResponse updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
    ApiAssert.isTrue(staffService.updateStatus(id, request.status()), ApiCode.NOT_FOUND, "员工不存在");
    return ApiResponse.ok(true);
  }
}
