package com.github.motoryang.system.modules.dept.service.impl;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.common.utils.SecurityUtils;
import com.github.motoryang.system.modules.dept.entity.Dept;
import com.github.motoryang.system.modules.dept.mapper.DeptMapper;
import com.github.motoryang.system.modules.dept.model.dto.DeptAssignUsersDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptRemoveUsersDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptUserQueryDTO;
import com.github.motoryang.system.modules.dept.model.dto.SetPositionDTO;
import com.github.motoryang.system.modules.dept.model.dto.SetPrimaryDeptDTO;
import com.github.motoryang.system.modules.dept.service.IDeptUserService;
import com.github.motoryang.system.modules.relation.entity.UserDept;
import com.github.motoryang.system.modules.relation.mapper.UserDeptMapper;
import com.github.motoryang.system.modules.user.converter.UserConverter;
import com.github.motoryang.system.modules.user.entity.User;
import com.github.motoryang.system.modules.user.mapper.UserMapper;
import com.github.motoryang.system.modules.user.model.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门用户服务实现
 */
@Service
@RequiredArgsConstructor
public class DeptUserServiceImpl implements IDeptUserService {

    private static final String DEFAULT_TENANT_ID = "0";

    private final UserMapper userMapper;
    private final UserConverter userConverter;
    private final UserDeptMapper userDeptMapper;
    private final DeptMapper deptMapper;
    private final IdentifierGenerator identifierGenerator;

    @Override
    public IPage<UserVO> pageUsersByDept(DeptUserQueryDTO dto) {
        Page<User> page = new Page<>(dto.current(), dto.size());
        IPage<User> userPage = userMapper.selectUsersByDeptId(
                page,
                dto.deptId(),
                dto.username(),
                dto.nickname(),
                dto.status()
        );

        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userConverter.toVOList(userPage.getRecords()));
        return voPage;
    }

    @Override
    public IPage<UserVO> pageUnassignedUsers(DeptUserQueryDTO dto) {
        Page<User> page = new Page<>(dto.current(), dto.size());
        IPage<User> userPage = userMapper.selectUnassignedUsersByDeptId(
                page,
                dto.deptId(),
                dto.username(),
                dto.nickname()
        );

        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userConverter.toVOList(userPage.getRecords()));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsers(DeptAssignUsersDTO dto) {
        if (CollectionUtils.isEmpty(dto.userIds())) {
            return;
        }

        // 验证部门是否存在
        Dept dept = deptMapper.selectById(dto.deptId());
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 获取已分配的用户ID
        List<String> existingUserIds = userDeptMapper.selectUserIdsByDeptId(dto.deptId());

        // 过滤出新用户
        List<String> newUserIds = dto.userIds().stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .toList();

        if (newUserIds.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String currentUserId = SecurityUtils.getUserIdOrNull();

        // 批量插入
        List<UserDept> userDepts = newUserIds.stream()
                .map(userId -> {
                    UserDept ud = new UserDept();
                    ud.setId(identifierGenerator.nextUUID(ud));
                    ud.setTenantId(DEFAULT_TENANT_ID);
                    ud.setUserId(userId);
                    ud.setDeptId(dto.deptId());
                    ud.setIsPrimary(0);
                    ud.setPosition(null);
                    ud.setCreateTime(now);
                    ud.setCreateBy(currentUserId);
                    return ud;
                })
                .toList();

        userDeptMapper.insertBatch(userDepts);

        // 如果用户没有主职部门，则设置为主职
        for (String userId : newUserIds) {
            String primaryDeptId = userDeptMapper.selectPrimaryDeptIdByUserId(userId);
            if (primaryDeptId == null) {
                userDeptMapper.setPrimary(userId, dto.deptId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUsers(DeptRemoveUsersDTO dto) {
        if (CollectionUtils.isEmpty(dto.userIds())) {
            return;
        }

        userDeptMapper.deleteBatchByDeptIdAndUserIds(dto.deptId(), dto.userIds());

        // 如果移除的是主职部门，需要重新设置主职
        for (String userId : dto.userIds()) {
            List<UserDept> remaining = userDeptMapper.selectByUserId(userId);
            if (!remaining.isEmpty()) {
                // 检查是否还有主职
                boolean hasPrimary = remaining.stream().anyMatch(ud -> ud.getIsPrimary() == 1);
                if (!hasPrimary) {
                    // 将第一个部门设置为主职
                    userDeptMapper.setPrimary(userId, remaining.get(0).getDeptId());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPrimaryDept(SetPrimaryDeptDTO dto) {
        // 验证关联是否存在
        UserDept userDept = userDeptMapper.selectByUserIdAndDeptId(dto.userId(), dto.deptId());
        if (userDept == null) {
            throw new BusinessException("用户不属于该部门");
        }

        // 清除原有主职
        userDeptMapper.clearPrimaryByUserId(dto.userId());

        // 设置新主职
        userDeptMapper.setPrimary(dto.userId(), dto.deptId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPosition(SetPositionDTO dto) {
        // 验证关联是否存在
        UserDept userDept = userDeptMapper.selectByUserIdAndDeptId(dto.userId(), dto.deptId());
        if (userDept == null) {
            throw new BusinessException("用户不属于该部门");
        }

        userDeptMapper.updatePosition(dto.userId(), dto.deptId(), dto.position());
    }
}
