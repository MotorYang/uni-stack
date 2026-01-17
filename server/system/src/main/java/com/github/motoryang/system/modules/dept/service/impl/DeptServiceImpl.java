package com.github.motoryang.system.modules.dept.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.system.modules.dept.converter.DeptConverter;
import com.github.motoryang.system.modules.dept.entity.Dept;
import com.github.motoryang.system.modules.dept.mapper.DeptMapper;
import com.github.motoryang.system.modules.dept.model.dto.DeptCreateDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptQueryDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptUpdateDTO;
import com.github.motoryang.system.modules.dept.model.vo.DeptVO;
import com.github.motoryang.system.modules.dept.service.IDeptService;
import com.github.motoryang.system.modules.user.entity.User;
import com.github.motoryang.system.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements IDeptService {

    private final DeptConverter deptConverter;
    private final UserMapper userMapper;

    @Override
    public List<DeptVO> listTree(DeptQueryDTO dto) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.deptName()), Dept::getDeptName, dto.deptName())
                .eq(dto.status() != null, Dept::getStatus, dto.status())
                .orderByAsc(Dept::getSort)
                .orderByAsc(Dept::getId);

        List<Dept> depts = list(wrapper);
        return buildTree(depts);
    }

    @Override
    public DeptVO getDeptDetail(String id) {
        Dept dept = getById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return deptConverter.toVO(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptVO createDept(DeptCreateDTO dto) {
        Dept dept = deptConverter.toEntity(dto);

        if (StringUtils.hasText(dto.parentId()) && !"0".equals(dto.parentId())) {
            Dept parent = getById(dto.parentId());
            if (parent == null) {
                throw new BusinessException("父部门不存在");
            }
            dept.setAncestors(parent.getAncestors() + "," + parent.getId());
        } else {
            dept.setAncestors("0");
        }

        save(dept);
        return deptConverter.toVO(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptVO updateDept(String id, DeptUpdateDTO dto) {
        Dept dept = getById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }

        // 不能将父部门设置为自己或自己的子部门
        if (dto.parentId() != null) {
            if (dto.parentId().equals(id)) {
                throw new BusinessException("父部门不能设置为自己");
            }
            // 检查是否是子部门
            Dept parent = getById(dto.parentId());
            if (parent != null && parent.getAncestors() != null && parent.getAncestors().contains("," + id + ",")) {
                throw new BusinessException("父部门不能设置为自己的子部门");
            }
        }

        // 更新祖级列表
        if (dto.parentId() != null && !dto.parentId().equals(dept.getParentId())) {
            String newAncestors;
            if ("0".equals(dto.parentId())) {
                newAncestors = "0";
            } else {
                Dept parent = getById(dto.parentId());
                newAncestors = parent.getAncestors() + "," + parent.getId();
            }
            // 更新子部门的祖级列表
            updateChildrenAncestors(id, dept.getAncestors(), newAncestors);
            dept.setAncestors(newAncestors);
        }

        deptConverter.updateEntity(dto, dept);
        updateById(dept);
        return deptConverter.toVO(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(String id) {
        // 检查是否有子部门
        long childCount = count(new LambdaQueryWrapper<Dept>()
                .eq(Dept::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子部门，不能删除");
        }

        // 检查是否有用户
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getDeptId, id));
        if (userCount > 0) {
            throw new BusinessException("部门下存在用户，不能删除");
        }

        removeById(id);
    }

    @Override
    public List<DeptVO> getDeptTreeSelect() {
        List<Dept> depts = list(new LambdaQueryWrapper<Dept>()
                .eq(Dept::getStatus, 0)
                .orderByAsc(Dept::getSort)
                .orderByAsc(Dept::getId));
        return buildTree(depts);
    }

    /**
     * 更新子部门的祖级列表
     */
    private void updateChildrenAncestors(String parentId, String oldAncestors, String newAncestors) {
        List<Dept> children = list(new LambdaQueryWrapper<Dept>()
                .apply("ancestors LIKE CONCAT({0}, ',', {1}, '%')", oldAncestors, parentId));

        for (Dept child : children) {
            child.setAncestors(child.getAncestors().replace(oldAncestors, newAncestors));
            updateById(child);
        }
    }

    /**
     * 构建部门树
     */
    private List<DeptVO> buildTree(List<Dept> depts) {
        if (depts == null || depts.isEmpty()) {
            return new ArrayList<>();
        }

        List<DeptVO> voList = deptConverter.toVOList(depts);

        Map<String, List<DeptVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(DeptVO::parentId));

        List<DeptVO> result = new ArrayList<>();
        for (DeptVO vo : voList) {
            if ("0".equals(vo.parentId())) {
                result.add(buildChildren(vo, parentMap));
            }
        }

        return result;
    }

    private DeptVO buildChildren(DeptVO parent, Map<String, List<DeptVO>> parentMap) {
        List<DeptVO> children = parentMap.get(parent.id());
        if (children == null || children.isEmpty()) {
            return parent;
        }

        List<DeptVO> childrenWithSub = children.stream()
                .map(child -> buildChildren(child, parentMap))
                .toList();

        return new DeptVO(
                parent.id(),
                parent.parentId(),
                parent.ancestors(),
                parent.deptName(),
                parent.sort(),
                parent.leader(),
                parent.phone(),
                parent.email(),
                parent.status(),
                parent.createTime(),
                childrenWithSub
        );
    }
}
