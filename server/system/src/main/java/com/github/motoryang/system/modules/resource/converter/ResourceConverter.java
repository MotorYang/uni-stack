package com.github.motoryang.system.modules.resource.converter;

import com.github.motoryang.system.modules.resource.entity.Resource;
import com.github.motoryang.system.modules.resource.model.dto.ResourceCreateDTO;
import com.github.motoryang.system.modules.resource.model.dto.ResourceUpdateDTO;
import com.github.motoryang.system.modules.resource.model.vo.ResourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 资源对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResourceConverter {

    ResourceConverter INSTANCE = Mappers.getMapper(ResourceConverter.class);

    ResourceVO toVO(Resource resource);

    List<ResourceVO> toVOList(List<Resource> resources);

    Resource toEntity(ResourceCreateDTO dto);

    void updateEntity(ResourceUpdateDTO dto, @MappingTarget Resource resource);
}
