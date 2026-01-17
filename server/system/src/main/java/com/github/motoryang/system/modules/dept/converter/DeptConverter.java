package com.github.motoryang.system.modules.dept.converter;

import com.github.motoryang.system.modules.dept.entity.Dept;
import com.github.motoryang.system.modules.dept.model.dto.DeptCreateDTO;
import com.github.motoryang.system.modules.dept.model.dto.DeptUpdateDTO;
import com.github.motoryang.system.modules.dept.model.vo.DeptVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 部门对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeptConverter {

    DeptConverter INSTANCE = Mappers.getMapper(DeptConverter.class);

    DeptVO toVO(Dept dept);

    List<DeptVO> toVOList(List<Dept> depts);

    Dept toEntity(DeptCreateDTO dto);

    void updateEntity(DeptUpdateDTO dto, @MappingTarget Dept dept);
}
