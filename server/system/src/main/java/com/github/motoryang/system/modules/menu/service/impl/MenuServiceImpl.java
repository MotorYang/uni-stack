package com.github.motoryang.system.modules.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.system.modules.menu.converter.MenuConverter;
import com.github.motoryang.system.modules.menu.entity.Menu;
import com.github.motoryang.system.modules.menu.mapper.MenuMapper;
import com.github.motoryang.system.modules.menu.model.dto.MenuCreateDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuQueryDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuUpdateDTO;
import com.github.motoryang.system.modules.menu.model.vo.MenuVO;
import com.github.motoryang.system.modules.menu.service.IMenuService;
import com.github.motoryang.system.modules.relation.mapper.RoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements IMenuService {

    private final MenuConverter menuConverter;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuVO> listTree(MenuQueryDTO dto) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.menuName()), Menu::getMenuName, dto.menuName())
                .eq(StringUtils.hasText(dto.menuType()), Menu::getMenuType, dto.menuType())
                .eq(dto.status() != null, Menu::getStatus, dto.status())
                .orderByAsc(Menu::getSort)
                .orderByAsc(Menu::getId);

        List<Menu> menus = list(wrapper);
        return buildTree(menus);
    }

    @Override
    public List<MenuVO> getMenuTreeByUserId(String userId) {
        List<Menu> menus = baseMapper.selectMenusByUserId(userId);
        return buildTree(menus);
    }

    @Override
    public MenuVO getMenuDetail(String id) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menuConverter.toVO(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuVO createMenu(MenuCreateDTO dto) {
        Menu menu = menuConverter.toEntity(dto);
        save(menu);
        return menuConverter.toVO(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuVO updateMenu(String id, MenuUpdateDTO dto) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }

        // 不能将父菜单设置为自己或自己的子菜单
        if (dto.parentId() != null && dto.parentId().equals(id)) {
            throw new BusinessException("父菜单不能设置为自己");
        }

        menuConverter.updateEntity(dto, menu);
        updateById(menu);
        return menuConverter.toVO(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(String id) {
        // 检查是否有子菜单
        long childCount = count(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，不能删除");
        }

        // 删除角色菜单关联
        roleMenuMapper.deleteByMenuId(id);

        // 逻辑删除菜单
        removeById(id);
    }

    @Override
    public List<MenuVO> getRoleMenuTree(String roleId) {
        List<Menu> allMenus = list(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getStatus, 0)
                .orderByAsc(Menu::getSort)
                .orderByAsc(Menu::getId));
        return buildTree(allMenus);
    }

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为 VO
        List<MenuVO> voList = menuConverter.toVOList(menus);

        // 按父ID分组
        Map<String, List<MenuVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(MenuVO::parentId));

        // 设置子菜单
        List<MenuVO> result = new ArrayList<>();
        for (MenuVO vo : voList) {
            if ("0".equals(vo.parentId())) {
                result.add(buildChildren(vo, parentMap));
            }
        }

        return result;
    }

    /**
     * 递归构建子菜单
     */
    private MenuVO buildChildren(MenuVO parent, Map<String, List<MenuVO>> parentMap) {
        List<MenuVO> children = parentMap.get(parent.id());
        if (children == null || children.isEmpty()) {
            return parent;
        }

        List<MenuVO> childrenWithSub = children.stream()
                .map(child -> buildChildren(child, parentMap))
                .toList();

        return new MenuVO(
                parent.id(),
                parent.parentId(),
                parent.menuName(),
                parent.menuType(),
                parent.path(),
                parent.component(),
                parent.perms(),
                parent.icon(),
                parent.sort(),
                parent.visible(),
                parent.status(),
                parent.createTime(),
                childrenWithSub
        );
    }
}
