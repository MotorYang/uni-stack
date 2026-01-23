package com.github.motoryang.system.modules.dept.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.dept.model.dto.DeptAssignUsersDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptRemoveUsersDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptUserQueryDTO;
import com.github.motoryang.system.modules.dept.model.dto.SetPositionDTO;
import com.github.motoryang.system.modules.dept.model.dto.SetPrimaryDeptDTO;
import com.github.motoryang.system.modules.dept.service.IDeptUserService;
import com.github.motoryang.system.modules.user.model.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 部门用户管理控制器
 *
 * @author motoryang
 */
@Tag(name = "部门用户管理", description = "部门用户的分配、移除、设置主职、设置职务等操作")
@RestController
@RequestMapping("/depts")
@RequiredArgsConstructor
public class DeptUserController {

    private final IDeptUserService deptUserService;

    /**
     * 分页查询部门下的用户
     *
     * @param deptId 部门ID
     * @param dto    查询条件
     * @return 用户分页列表
     */
    @Operation(summary = "分页查询部门用户", description = "分页查询指定部门下的用户列表")
    @GetMapping("/{deptId}/users")
    public RestResult<IPage<UserVO>> pageUsersByDept(
            @Parameter(description = "部门ID", required = true) @PathVariable String deptId,
            DeptUserQueryDTO dto) {
        DeptUserQueryDTO queryDTO = new DeptUserQueryDTO(
                deptId,
                dto.username(),
                dto.nickname(),
                dto.status(),
                dto.current(),
                dto.size()
        );
        return RestResult.ok(deptUserService.pageUsersByDept(queryDTO));
    }

    /**
     * 分页查询未分配到部门的用户
     *
     * @param deptId 部门ID
     * @param dto    查询条件
     * @return 用户分页列表
     */
    @Operation(summary = "查询未分配用户", description = "分页查询未分配到指定部门的用户列表")
    @GetMapping("/{deptId}/unassigned-users")
    public RestResult<IPage<UserVO>> pageUnassignedUsers(
            @Parameter(description = "部门ID", required = true) @PathVariable String deptId,
            DeptUserQueryDTO dto) {
        DeptUserQueryDTO queryDTO = new DeptUserQueryDTO(
                deptId,
                dto.username(),
                dto.nickname(),
                dto.status(),
                dto.current(),
                dto.size()
        );
        return RestResult.ok(deptUserService.pageUnassignedUsers(queryDTO));
    }

    /**
     * 部门分配用户
     *
     * @param dto    分配参数
     * @return 操作结果
     */
    @Operation(summary = "部门分配用户", description = "将用户分配到指定部门")
    @PostMapping("/assign/users")
    public RestResult<Void> assignUsers(@Valid @RequestBody DeptAssignUsersDTO dto) {
        deptUserService.assignUsers(dto);
        return RestResult.ok();
    }

    /**
     * 部门移除用户
     *
     * @param deptId 部门ID
     * @param dto    移除参数
     * @return 操作结果
     */
    @Operation(summary = "部门移除用户", description = "从指定部门移除用户")
    @DeleteMapping("/{deptId}/users")
    public RestResult<Void> removeUsers(
            @Parameter(description = "部门ID", required = true) @PathVariable String deptId,
            @Valid @RequestBody DeptRemoveUsersDTO dto) {
        DeptRemoveUsersDTO removeDTO = new DeptRemoveUsersDTO(deptId, dto.userIds());
        deptUserService.removeUsers(removeDTO);
        return RestResult.ok();
    }

    /**
     * 设置用户主职部门
     *
     * @param dto 设置参数
     * @return 操作结果
     */
    @Operation(summary = "设置主职部门", description = "设置用户的主职部门")
    @PutMapping("/users/primary")
    public RestResult<Void> setPrimaryDept(@Valid @RequestBody SetPrimaryDeptDTO dto) {
        deptUserService.setPrimaryDept(dto);
        return RestResult.ok();
    }

    /**
     * 设置用户在部门中的职务
     *
     * @param dto 设置参数
     * @return 操作结果
     */
    @Operation(summary = "设置职务", description = "设置用户在指定部门中的职务")
    @PutMapping("/users/position")
    public RestResult<Void> setPosition(@Valid @RequestBody SetPositionDTO dto) {
        deptUserService.setPosition(dto);
        return RestResult.ok();
    }
}
