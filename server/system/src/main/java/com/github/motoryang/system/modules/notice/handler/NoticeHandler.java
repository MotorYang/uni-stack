package com.github.motoryang.system.modules.notice.handler;

import com.github.motoryang.common.message.utils.MessageHelper;
import com.github.motoryang.system.modules.notice.entity.Notice;
import com.github.motoryang.system.modules.notice.mapper.NoticeTargetMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 通知操作处理器
 */
@Component
@AllArgsConstructor
public class NoticeHandler {

    private final MessageHelper messageHelper;
    private final NoticeTargetMapper targetMapper;

    /**
     * 发布通知
     * @param notice 通知对象
     */
    public void publishNotice(Notice notice) {
        String noticeType = notice.getType();
        switch (noticeType) {
            case "NOTICE":
                // TODO: 通知
                break;
            case "ANNOUNCE":
                sendAnnounce(notice);
                break;
            case "TASK":
                // TODO: 待办
                break;
            default:
                throw new IllegalArgumentException("未知的通知类型: " + noticeType);
        }
    }

    /**
     * 撤销通知
     * @param notice 通知对象
     */
    public void revokeNotice(Notice notice) {

    }

    /**
     * 发送公告
     * @param notice 通知对象
     */
    private void sendAnnounce(Notice notice) {
        switch (notice.getTarget()) {
            case "ALL":
                messageHelper.broadcast("announce", notice.getContent());
                break;
            case "ROLE":
                // TODO: 给特定角色发送通知
                break;
            case "USER":
                // TODO: 给特定用户发送通知
                break;
            default:
                throw new IllegalArgumentException("未知的通知目标: " + notice.getTarget());
        }
    }

}
