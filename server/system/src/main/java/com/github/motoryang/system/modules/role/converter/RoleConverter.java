package com.github.motoryang.system.modules.role.converter;

import com.github.motoryang.system.modules.role.entity.Role;
import com.github.motoryang.system.modules.role.model.dto.RoleCreateDTO;
import com.github.motoryang.system.modules.role.model.dto.RoleUpdateDTO;
import com.github.motoryang.system.modules.role.model.vo.RoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    RoleVO toVO(Role role);

    List<RoleVO> toVOList(List<Role> roles);

    Role toEntity(RoleCreateDTO dto);

    void updateEntity(RoleUpdateDTO dto, @MappingTarget Role role);
}
