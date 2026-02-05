package com.github.motoryang.system.modules.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.motoryang.system.modules.notice.entity.Notice;
import com.github.motoryang.system.modules.notice.model.dto.NoticeCreateDTO;
import com.github.motoryang.system.modules.notice.model.dto.NoticeQueryDTO;
import com.github.motoryang.system.modules.notice.model.dto.NoticeUpdateDTO;
import com.github.motoryang.system.modules.notice.model.vo.NoticeDetailVO;
import com.github.motoryang.system.modules.notice.model.vo.NoticeVO;

/**
 * 通知服务接口
 */
public interface INoticeService extends IService<Notice> {

    /**
     * 分页查询通知
     */
    IPage<NoticeVO> pageQuery(NoticeQueryDTO dto);

    /**
     * 获取通知详情
     */
    NoticeDetailVO getNoticeDetail(String id);

    /**
     * 创建通知（草稿）
     */
    NoticeVO createNotice(NoticeCreateDTO dto);

    /**
     * 更新通知
     */
    NoticeVO updateNotice(String id, NoticeUpdateDTO dto);

    /**
     * 删除通知
     */
    void deleteNotice(String id);

    /**
     * 发布通知
     */
    void publishNotice(String id);

    /**
     * 撤销通知
     */
    void revokeNotice(String id);
}
