package com.github.motoryang.system.modules.menu.converter;

import com.github.motoryang.system.modules.menu.entity.Menu;
import com.github.motoryang.system.modules.menu.model.dto.MenuCreateDTO;
import com.github.motoryang.system.modules.menu.model.dto.MenuUpdateDTO;
import com.github.motoryang.system.modules.menu.model.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 菜单对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MenuConverter {

    MenuConverter INSTANCE = Mappers.getMapper(MenuConverter.class);

    MenuVO toVO(Menu menu);

    List<MenuVO> toVOList(List<Menu> menus);

    Menu toEntity(MenuCreateDTO dto);

    void updateEntity(MenuUpdateDTO dto, @MappingTarget Menu menu);
}
