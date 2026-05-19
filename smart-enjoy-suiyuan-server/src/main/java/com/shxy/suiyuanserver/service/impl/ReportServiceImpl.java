package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.dto.ReportDTO;
import com.shxy.suiyuanentity.entity.Comment;
import com.shxy.suiyuanentity.entity.Post;
import com.shxy.suiyuanentity.entity.Report;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanserver.mapper.CommentMapper;
import com.shxy.suiyuanserver.mapper.PostMapper;
import com.shxy.suiyuanserver.mapper.ReportMapper;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Slf4j
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report>
        implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    private static final Set<String> VALID_TARGET_TYPES = Set.of("post", "comment", "user");
    private static final int AUTO_TAKE_DOWN_THRESHOLD = 10;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<Report> submitReport(ReportDTO reportDTO) {
        Long currentUserId = BaseContext.getCurrentUserId();

        if (!VALID_TARGET_TYPES.contains(reportDTO.getTargetType())) {
            throw new BaseException("举报对象类型不合法，仅支持: post, comment, user");
        }

        if (reportDTO.getTargetId() == null || reportDTO.getTargetId() <= 0) {
            throw new BaseException("举报对象ID不合法");
        }

        LambdaQueryWrapper<Report> existsWrapper = new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, currentUserId)
                .eq(Report::getTargetType, reportDTO.getTargetType())
                .eq(Report::getTargetId, reportDTO.getTargetId())
                .eq(Report::getStatus, 0);
        if (reportMapper.exists(existsWrapper)) {
            throw new BaseException("您已举报过该内容，请等待处理");
        }

        Report report = Report.builder()
                .reporterId(currentUserId)
                .targetType(reportDTO.getTargetType())
                .targetId(reportDTO.getTargetId())
                .reasonType(reportDTO.getReasonType())
                .reasonDetail(reportDTO.getReasonDetail())
                .status(0)
                .createTime(new Date())
                .build();
        int insert = reportMapper.insert(report);
        if (insert <= 0) {
            throw new BaseException("举报提交失败");
        }
        long reportCount = reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getTargetType, reportDTO.getTargetType())
                .eq(Report::getTargetId, reportDTO.getTargetId())
                .ne(Report::getStatus, 2));
        if (reportCount >= AUTO_TAKE_DOWN_THRESHOLD) {
            // 执行下架、封号逻辑
            handleAutoTakeDown(reportDTO.getTargetType(), reportDTO.getTargetId());
            log.warn("举报数量达到阈值，自动下架: targetType={}, targetId={}, reportCount={}",
                    reportDTO.getTargetType(), reportDTO.getTargetId(), reportCount);
        }
        log.info("用户{}举报成功: targetType={}, targetId={}, reasonType={}",
                currentUserId, reportDTO.getTargetType(), reportDTO.getTargetId(), reportDTO.getReasonType());
        return Result.success(report);
    }

    /**
     * 处理自动下架逻辑
     * @param targetType 目标类型 (post, comment, user)
     * @param targetId 目标ID
     */
    private void handleAutoTakeDown(String targetType, Long targetId) {
        try {
            switch (targetType.toLowerCase()) {
                case "post":
                    Post post = new Post();
                    post.setId(targetId);
                    post.setStatus(2); // 2-已锁定
                    post.setUpdateTime(new Date());
                    postMapper.updateById(post);
                    log.info("自动下架帖子成功: postId={}", targetId);
                    break;
                    
                case "comment":
                    Comment comment = new Comment();
                    comment.setId(targetId);
                    comment.setIsDeleted(1); // 1-已删除
                    comment.setUpdateTime(new Date());
                    commentMapper.updateById(comment);
                    log.info("自动下架评论成功: commentId={}", targetId);
                    break;
                    
                case "user":
                    User user = new User();
                    user.setId(targetId);
                    user.setStatus(0); // 0-禁用(封号)
                    user.setUpdateTime(new Date());
                    userMapper.updateById(user);
                    log.info("自动封禁用户成功: userId={}", targetId);
                    break;
                    
                default:
                    log.warn("不支持的目标类型: {}", targetType);
                    break;
            }
        } catch (Exception e) {
            log.error("自动下架处理失败: targetType={}, targetId={}, error={}", 
                    targetType, targetId, e.getMessage(), e);
            throw new BaseException("自动下架处理失败: " + e.getMessage());
        }
    }
}
