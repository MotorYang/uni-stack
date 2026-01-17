package com.github.motoryang.system.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.system.modules.user.model.dto.UserCreateDTO;
import com.github.motoryang.system.modules.user.model.dto.UserQueryDTO;
import com.github.motoryang.system.modules.user.model.dto.UserUpdateDTO;
import com.github.motoryang.system.modules.user.model.vo.UserDetailVO;
import com.github.motoryang.system.modules.user.model.vo.UserVO;
import com.github.motoryang.system.modules.user.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户
     */
    @GetMapping
    public RestResult<IPage<UserVO>> page(UserQueryDTO dto) {
        return RestResult.ok(userService.pageQuery(dto));
    }

    @GetMapping("/{id}")
    public RestResult<UserDetailVO> getDetail(@PathVariable String id) {
        return RestResult.ok(userService.getUserDetail(id));
    }

    /**
     * 创建用户
     */
    @PostMapping
    public RestResult<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return RestResult.ok(userService.createUser(dto));
    }

    @PutMapping("/{id}")
    public RestResult<UserVO> update(@PathVariable String id, @Valid @RequestBody UserUpdateDTO dto) {
        return RestResult.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public RestResult<Void> delete(@PathVariable String id) {
        userService.deleteUser(id);
        return RestResult.ok();
    }

    @PutMapping("/{id}/reset-password")
    public RestResult<Void> resetPassword(@PathVariable String id, @RequestBody String newPassword) {
        userService.resetPassword(id, newPassword);
        return RestResult.ok();
    }
}
