package com.github.motoryang.system.modules.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.motoryang.system.modules.notice.entity.NoticeTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知目标 Mapper
 */
@Mapper
public interface NoticeTargetMapper extends BaseMapper<NoticeTarget> {

    /**
     * 批量插入通知目标
     */
    int insertBatch(@Param("list") List<NoticeTarget> list);

    /**
     * 根据通知ID删除目标
     */
    int deleteByNoticeId(@Param("noticeId") String noticeId);

    /**
     * 根据通知ID查询目标列表
     */
    List<NoticeTarget> selectByNoticeId(@Param("noticeId") String noticeId);
}
