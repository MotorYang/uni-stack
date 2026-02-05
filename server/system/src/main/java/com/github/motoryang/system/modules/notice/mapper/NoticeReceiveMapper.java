package com.github.motoryang.system.modules.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.notice.entity.NoticeReceive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知接收 Mapper
 */
@Mapper
public interface NoticeReceiveMapper extends BaseMapper<NoticeReceive> {

    /**
     * 批量插入通知接收记录
     */
    int insertBatch(@Param("list") List<NoticeReceive> list);

    /**
     * 根据通知ID删除接收记录
     */
    int deleteByNoticeId(@Param("noticeId") String noticeId);
}
