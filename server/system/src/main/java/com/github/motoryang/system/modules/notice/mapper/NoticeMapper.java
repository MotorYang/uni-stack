package com.github.motoryang.system.modules.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知 Mapper
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
