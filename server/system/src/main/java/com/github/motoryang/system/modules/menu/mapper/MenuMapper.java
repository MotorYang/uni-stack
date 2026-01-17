package com.github.motoryang.system.modules.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.menu.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据用户ID查询菜单树
     */
    List<Menu> selectMenusByUserId(@Param("userId") String userId);

    /**
     * 查询所有菜单树
     */
    List<Menu> selectAllMenus();
}
