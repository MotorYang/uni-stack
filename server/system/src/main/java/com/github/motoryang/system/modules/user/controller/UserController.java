package com.github.motoryang.system.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.user.model.dto.UserCreateDTO;
import com.github.motoryang.system.modules.user.model.dto.UserQueryDTO;
import com.github.motoryang.system.modules.user.model.dto.UserUpdateDTO;
import com.github.motoryang.system.modules.user.model.vo.UserDetailVO;
import com.github.motoryang.system.modules.user.model.vo.UserVO;
import com.github.motoryang.system.modules.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * @author motoryang
 */
@Tag(name = "用户管理", description = "用户的增删改查、密码重置等操作")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户
     *
     * @param dto 查询条件
     * @return 用户分页列表
     */
    @Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表")
    @GetMapping
    public RestResult<IPage<UserVO>> page(UserQueryDTO dto) {
        return RestResult.ok(userService.pageQuery(dto));
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息，包含角色信息")
    @GetMapping("/{id}")
    public RestResult<UserDetailVO> getDetail(
            @Parameter(description = "用户ID", required = true) @PathVariable String id) {
        return RestResult.ok(userService.getUserDetail(id));
    }

    /**
     * 创建用户
     *
     * @param dto 用户创建参数
     * @return 创建的用户信息
     */
    @Operation(summary = "创建用户", description = "新增一个用户")
    @PostMapping
    public RestResult<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return RestResult.ok(userService.createUser(dto));
    }

    /**
     * 更新用户
     *
     * @param id  用户ID
     * @param dto 用户更新参数
     * @return 更新后的用户信息
     */
    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    @PutMapping("/{id}")
    public RestResult<UserVO> update(
            @Parameter(description = "用户ID", required = true) @PathVariable String id,
            @Valid @RequestBody UserUpdateDTO dto) {
        return RestResult.ok(userService.updateUser(id, dto));
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户", description = "根据用户ID删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    public RestResult<Void> delete(
            @Parameter(description = "用户ID", required = true) @PathVariable String id) {
        userService.deleteUser(id);
        return RestResult.ok();
    }

    /**
     * 重置用户密码
     *
     * @param userId      用户ID
     * @param newPassword 新密码
     * @return 操作结果
     */
    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码")
    @PutMapping("/{userId}/reset-password")
    public RestResult<Void> resetPassword(
            @PathVariable @Parameter(description = "用户ID", required = true) String userId,
            @RequestBody String newPassword) {
        userService.resetPassword(userId, newPassword);
        return RestResult.ok();
    }
}
