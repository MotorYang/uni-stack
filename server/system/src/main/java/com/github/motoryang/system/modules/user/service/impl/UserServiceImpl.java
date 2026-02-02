package com.github.motoryang.system.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.system.modules.dept.entity.Dept;
import com.github.motoryang.system.modules.dept.mapper.DeptMapper;
import com.github.motoryang.system.modules.relation.entity.UserDept;
import com.github.motoryang.system.modules.relation.entity.UserRole;
import com.github.motoryang.system.modules.relation.mapper.UserDeptMapper;
import com.github.motoryang.system.modules.relation.mapper.UserRoleMapper;
import com.github.motoryang.system.modules.role.entity.Role;
import com.github.motoryang.system.modules.role.mapper.RoleMapper;
import com.github.motoryang.system.modules.user.converter.UserConverter;
import com.github.motoryang.system.modules.user.entity.User;
import com.github.motoryang.system.modules.user.mapper.UserMapper;
import com.github.motoryang.system.modules.user.model.dto.UserCreateDTO;
import com.github.motoryang.system.modules.user.model.dto.UserQueryDTO;
import com.github.motoryang.system.modules.user.model.dto.UserUpdateDTO;
import com.github.motoryang.system.modules.user.model.vo.UserDeptVO;
import com.github.motoryang.system.modules.user.model.vo.UserDetailVO;
import com.github.motoryang.system.modules.user.model.vo.UserVO;
import com.github.motoryang.system.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserConverter userConverter;
    private final UserRoleMapper userRoleMapper;
    private final UserDeptMapper userDeptMapper;
    private final RoleMapper roleMapper;
    private final DeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<UserVO> pageQuery(UserQueryDTO dto) {
        Page<User> page = new Page<>(dto.current(), dto.size());

        User queryUser = new User();
        queryUser.setUsername(dto.username());
        queryUser.setNickname(dto.nickname());
        queryUser.setPhone(dto.phone());
        queryUser.setStatus(dto.status());
        queryUser.setDeptId(dto.deptId());

        IPage<User> userPage = baseMapper.selectUserPage(page, queryUser);

        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userConverter.toVOList(userPage.getRecords()));
        return voPage;
    }

    @Override
    public UserDetailVO getUserDetail(String id) {
        User user = baseMapper.selectUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 获取用户角色
        List<String> roleIds = userRoleMapper.selectRoleIdsByUserId(id);
        List<String> roleKeys = new ArrayList<>();
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            roleKeys = roles.stream().map(Role::getRoleKey).toList();
        }

        // 获取用户权限
        List<String> permissions = roleMapper.selectPermissionsByUserId(id);

        // 获取用户所有部门关联
        List<UserDept> userDepts = userDeptMapper.selectByUserId(id);
        List<UserDeptVO> deptVOs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userDepts)) {
            List<String> deptIds = userDepts.stream().map(UserDept::getDeptId).toList();
            List<Dept> depts = deptMapper.selectBatchIds(deptIds);
            Map<String, Dept> deptMap = depts.stream()
                    .collect(Collectors.toMap(Dept::getId, d -> d));

            // 收集所有需要查询的祖级部门ID（用于查找所属公司）
            List<String> allAncestorIds = depts.stream()
                    .filter(d -> d.getAncestors() != null && !d.getAncestors().isEmpty())
                    .flatMap(d -> {
                        String[] ids = d.getAncestors().split(",");
                        return java.util.Arrays.stream(ids).filter(s -> !"0".equals(s));
                    })
                    .distinct()
                    .toList();

            // 查询所有祖级部门
            Map<String, Dept> ancestorDeptMap = new java.util.HashMap<>();
            if (!allAncestorIds.isEmpty()) {
                List<Dept> ancestorDepts = deptMapper.selectBatchIds(allAncestorIds);
                ancestorDeptMap = ancestorDepts.stream()
                        .collect(Collectors.toMap(Dept::getId, d -> d));
            }

            final Map<String, Dept> finalAncestorDeptMap = ancestorDeptMap;
            deptVOs = userDepts.stream()
                    .map(ud -> {
                        Dept dept = deptMap.get(ud.getDeptId());
                        String deptName = dept != null ? dept.getDeptName() : "";
                        String deptType = dept != null ? dept.getDeptType() : "D";

                        // 查找所属公司
                        String companyId = null;
                        String companyName = null;

                        if (dept != null) {
                            // 如果当前部门本身就是公司
                            if ("C".equals(deptType)) {
                                companyId = dept.getId();
                                companyName = dept.getDeptName();
                            } else if (dept.getAncestors() != null && !dept.getAncestors().isEmpty()) {
                                // 从祖级中查找最近的公司类型部门
                                String[] ancestorIds = dept.getAncestors().split(",");
                                // 从后往前找（最近的公司）
                                for (int i = ancestorIds.length - 1; i >= 0; i--) {
                                    String ancestorId = ancestorIds[i];
                                    if ("0".equals(ancestorId)) continue;
                                    Dept ancestor = finalAncestorDeptMap.get(ancestorId);
                                    if (ancestor != null && "C".equals(ancestor.getDeptType())) {
                                        companyId = ancestor.getId();
                                        companyName = ancestor.getDeptName();
                                        break;
                                    }
                                }
                            }
                        }

                        if (ud.getIsPrimary() == 1) {
                            user.setDeptId(ud.getDeptId());
                            user.setDeptName(deptName);
                        }
                        return new UserDeptVO(
                                ud.getDeptId(),
                                deptName,
                                deptType,
                                companyId,
                                companyName,
                                ud.getIsPrimary(),
                                ud.getPosition());
                    })
                    .toList();
        }

        return new UserDetailVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatar(),
                user.getGender(),
                user.getStatus(),
                user.getDeptId(),
                user.getDeptName(),
                user.getPosition(),
                deptVOs,
                roleIds,
                roleKeys,
                permissions,
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserCreateDTO dto) {
        // 检查用户名是否存在
        if (existsByUsername(dto.username())) {
            throw new BusinessException("用户名已存在");
        }

        User user = userConverter.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));

        save(user);

        // TODO: 暂时给所有新注册的用户给成默认角色，后期在前端可以配置默认角色
        // 保存用户角色关联
        if (CollectionUtils.isEmpty(dto.roleIds())) {
            saveUserRoles(user.getId(), List.of("019bd15d-d448-7323-a390-b3af5222b71e"));
        } else {
            saveUserRoles(user.getId(), dto.roleIds());
        }

        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(String id, UserUpdateDTO dto) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户名是否重复
        if (StringUtils.hasText(dto.username()) && !dto.username().equals(user.getUsername())) {
            if (existsByUsername(dto.username())) {
                throw new BusinessException("用户名已存在");
            }
        }

        userConverter.updateEntity(dto, user);
        updateById(user);

        // 更新用户角色关联
        if (dto.roleIds() != null) {
            userRoleMapper.deleteByUserId(id);
            if (!dto.roleIds().isEmpty()) {
                saveUserRoles(id, dto.roleIds());
            }
        }

        return userConverter.toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 删除用户角色关联
        userRoleMapper.deleteByUserId(id);

        // 删除用户部门关联
        userDeptMapper.deleteByUserId(id);

        // 逻辑删除用户
        removeById(id);
    }

    @Override
    public void resetPassword(String id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }

    private void saveUserRoles(String userId, List<String> roleIds) {
        List<UserRole> userRoles = roleIds.stream()
                .map(roleId -> {
                    UserRole ur = new UserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                })
                .toList();
        userRoleMapper.insertBatch(userRoles);
    }
}
