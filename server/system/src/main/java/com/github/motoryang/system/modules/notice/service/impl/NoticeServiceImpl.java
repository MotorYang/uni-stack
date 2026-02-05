package com.github.motoryang.system.modules.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.system.modules.notice.converter.NoticeConverter;
import com.github.motoryang.system.modules.notice.entity.Notice;
import com.github.motoryang.system.modules.notice.entity.NoticeTarget;
import com.github.motoryang.system.modules.notice.mapper.NoticeMapper;
import com.github.motoryang.system.modules.notice.mapper.NoticeReceiveMapper;
import com.github.motoryang.system.modules.notice.mapper.NoticeTargetMapper;
import com.github.motoryang.system.modules.notice.model.dto.NoticeCreateDTO;
import com.github.motoryang.system.modules.notice.model.dto.NoticeQueryDTO;
import com.github.motoryang.system.modules.notice.model.dto.NoticeUpdateDTO;
import com.github.motoryang.system.modules.notice.model.vo.NoticeDetailVO;
import com.github.motoryang.system.modules.notice.model.vo.NoticeTargetVO;
import com.github.motoryang.system.modules.notice.model.vo.NoticeVO;
import com.github.motoryang.system.modules.notice.service.INoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知服务实现
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

    private final NoticeConverter noticeConverter;
    private final NoticeTargetMapper noticeTargetMapper;
    private final NoticeReceiveMapper noticeReceiveMapper;

    @Override
    public IPage<NoticeVO> pageQuery(NoticeQueryDTO dto) {
        Page<Notice> page = new Page<>(dto.current(), dto.size());

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.title()), Notice::getTitle, dto.title())
                .eq(StringUtils.hasText(dto.type()), Notice::getType, dto.type())
                .eq(StringUtils.hasText(dto.status()), Notice::getStatus, dto.status())
                .eq(StringUtils.hasText(dto.priority()), Notice::getPriority, dto.priority())
                .orderByDesc(Notice::getCreateTime);

        IPage<Notice> noticePage = page(page, wrapper);

        Page<NoticeVO> voPage = new Page<>(noticePage.getCurrent(), noticePage.getSize(), noticePage.getTotal());
        voPage.setRecords(noticeConverter.toVOList(noticePage.getRecords()));
        return voPage;
    }

    @Override
    public NoticeDetailVO getNoticeDetail(String id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        // 查询通知目标
        List<NoticeTarget> targets = noticeTargetMapper.selectByNoticeId(id);
        List<NoticeTargetVO> targetVOs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(targets)) {
            targetVOs = targets.stream()
                    .map(t -> new NoticeTargetVO(t.getId(), t.getNoticeType(), t.getTargetId(), null))
                    .toList();
        }

        return new NoticeDetailVO(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getType(),
                notice.getPriority(),
                notice.getStatus(),
                notice.getTarget(),
                notice.getTimeOption(),
                notice.getNoticeTime(),
                targetVOs,
                notice.getCreateBy(),
                notice.getCreateTime(),
                notice.getUpdateTime()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO createNotice(NoticeCreateDTO dto) {
        Notice notice = noticeConverter.toEntity(dto);
        notice.setStatus("DRAFT");

        if (!StringUtils.hasText(dto.priority())) {
            notice.setPriority("M");
        }
        if (!StringUtils.hasText(dto.timeOption())) {
            notice.setTimeOption("NOW");
        }

        save(notice);

        // 保存通知目标
        saveNoticeTargets(notice.getId(), dto.target(), dto.targetIds());

        return noticeConverter.toVO(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO updateNotice(String id, NoticeUpdateDTO dto) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        if ("PUBLISHED".equals(notice.getStatus())) {
            throw new BusinessException("已发布的通知不能修改");
        }

        noticeConverter.updateEntity(dto, notice);
        updateById(notice);

        // 更新通知目标
        if (dto.targetIds() != null) {
            noticeTargetMapper.deleteByNoticeId(id);
            String target = StringUtils.hasText(dto.target()) ? dto.target() : notice.getTarget();
            saveNoticeTargets(id, target, dto.targetIds());
        }

        return noticeConverter.toVO(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(String id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        // 删除通知目标
        noticeTargetMapper.deleteByNoticeId(id);
        // 删除通知接收记录
        noticeReceiveMapper.deleteByNoticeId(id);
        // 逻辑删除通知
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishNotice(String id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        if (!"DRAFT".equals(notice.getStatus())) {
            throw new BusinessException("只有草稿状态的通知才能发布");
        }

        notice.setStatus("PUBLISHED");
        updateById(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeNotice(String id) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("通知不存在");
        }

        if (!"PUBLISHED".equals(notice.getStatus())) {
            throw new BusinessException("只有已发布的通知才能撤销");
        }

        notice.setStatus("REVOKED");
        updateById(notice);
    }

    private void saveNoticeTargets(String noticeId, String target, List<String> targetIds) {
        if ("ALL".equals(target) || CollectionUtils.isEmpty(targetIds)) {
            return;
        }

        String noticeType = "ROLE".equals(target) ? "ROLE" : "USER";
        List<NoticeTarget> targets = targetIds.stream()
                .map(targetId -> {
                    NoticeTarget nt = new NoticeTarget();
                    nt.setNoticeId(noticeId);
                    nt.setNoticeType(noticeType);
                    nt.setTargetId(targetId);
                    return nt;
                })
                .toList();
        noticeTargetMapper.insertBatch(targets);
    }
}
