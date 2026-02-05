package com.github.motoryang.system.modules.notice.converter;

import com.github.motoryang.system.modules.notice.entity.Notice;
import com.github.motoryang.system.modules.notice.model.dto.NoticeCreateDTO;
import com.github.motoryang.system.modules.notice.model.dto.NoticeUpdateDTO;
import com.github.motoryang.system.modules.notice.model.vo.NoticeVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 通知对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NoticeConverter {

    NoticeConverter INSTANCE = Mappers.getMapper(NoticeConverter.class);

    NoticeVO toVO(Notice notice);

    List<NoticeVO> toVOList(List<Notice> notices);

    Notice toEntity(NoticeCreateDTO dto);

    void updateEntity(NoticeUpdateDTO dto, @MappingTarget Notice notice);
}
