package com.github.motoryang.system.modules.user.converter;

import com.github.motoryang.system.modules.user.entity.User;
import com.github.motoryang.system.modules.user.model.dto.UserCreateDTO;
import com.github.motoryang.system.modules.user.model.dto.UserUpdateDTO;
import com.github.motoryang.system.modules.user.model.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    /**
     * Entity -> VO
     */
    UserVO toVO(User user);

    /**
     * Entity List -> VO List
     */
    List<UserVO> toVOList(List<User> users);

    /**
     * CreateDTO -> Entity
     */
    User toEntity(UserCreateDTO dto);

    /**
     * UpdateDTO -> Entity (更新)
     */
    void updateEntity(UserUpdateDTO dto, @MappingTarget User user);
}
