package com.github.motoryang.system.modules.resource.converter;

import com.github.motoryang.system.modules.resource.entity.ResourceGroup;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupCreateDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceGroupUpdateDTO;
import com.github.motoryang.system.modules.resource.model.vo.ResourceGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 资源组对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResourceGroupConverter {

    ResourceGroupConverter INSTANCE = Mappers.getMapper(ResourceGroupConverter.class);

    @Mapping(target = "resourceCount", ignore = true)
    ResourceGroupVO toVO(ResourceGroup resourceGroup);

    List<ResourceGroupVO> toVOList(List<ResourceGroup> resourceGroups);

    ResourceGroup toEntity(ResourceGroupCreateDTO dto);

    void updateEntity(ResourceGroupUpdateDTO dto, @MappingTarget ResourceGroup resourceGroup);
}
