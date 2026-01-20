package com.github.motoryang.system.modules.permission.converter;

import com.github.motoryang.system.modules.permission.entity.Permission;
import com.github.motoryang.system.modules.permission.model.dto.PermissionCreateDTO;
import com.github.motoryang.system.modules.permission.model.dto.PermissionUpdateDTO;
import com.github.motoryang.system.modules.permission.model.vo.PermissionVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 权限对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionConverter {

    PermissionConverter INSTANCE = Mappers.getMapper(PermissionConverter.class);

    PermissionVO toVO(Permission permission);

    List<PermissionVO> toVOList(List<Permission> permissions);

    Permission toEntity(PermissionCreateDTO dto);

    void updateEntity(PermissionUpdateDTO dto, @MappingTarget Permission permission);
}
