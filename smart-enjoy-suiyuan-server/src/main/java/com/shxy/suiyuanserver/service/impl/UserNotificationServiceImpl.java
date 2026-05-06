package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.entity.UserNotification;
import com.shxy.suiyuanentity.vo.NotificationStatsVO;
import com.shxy.suiyuanentity.vo.NotificationVO;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.mapper.UserNotificationMapper;
import com.shxy.suiyuanserver.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 用户通知Service实现
 */
@Slf4j
@Service
public class UserNotificationServiceImpl extends ServiceImpl<UserNotificationMapper, UserNotification>
        implements UserNotificationService {

    private final UserNotificationMapper userNotificationMapper;
    private final UserMapper userMapper;

    public UserNotificationServiceImpl(UserNotificationMapper userNotificationMapper, UserMapper userMapper) {
        this.userNotificationMapper = userNotificationMapper;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public Result<PageResult> getNotifications(Integer page, Integer size) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        int pageNum = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 20 : Math.min(size, 50);
        int offset = (pageNum - 1) * pageSize;

        List<NotificationVO> notifications = userNotificationMapper.selectNotificationList(userId, offset, pageSize);
        Long total = userNotificationMapper.countByUserId(userId);

        return Result.success(PageResult.builder()
                .total(total)
                .page(pageNum)
                .size(pageSize)
                .records(notifications)
                .build());
    }

    @Override
    public Result<String> markAsRead(Long notificationId) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        UserNotification notification = userNotificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return Result.fail("通知不存在");
        }

        userNotificationMapper.update(null,
                new LambdaUpdateWrapper<UserNotification>()
                        .eq(UserNotification::getId, notificationId)
                        .eq(UserNotification::getUserId, userId)
                        .eq(UserNotification::getIsRead, 0)
                        .set(UserNotification::getIsRead, 1)
                        .set(UserNotification::getReadTime, new Date()));

        return Result.success("已标记为已读");
    }

    @Override
    public Result<String> markAllAsRead() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        userNotificationMapper.update(null,
                new LambdaUpdateWrapper<UserNotification>()
                        .eq(UserNotification::getUserId, userId)
                        .eq(UserNotification::getIsRead, 0)
                        .set(UserNotification::getIsRead, 1)
                        .set(UserNotification::getReadTime, new Date()));

        return Result.success("已全部标记为已读");
    }

    @Override
    public Result<String> deleteNotification(Long notificationId) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        UserNotification notification = userNotificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return Result.fail("通知不存在");
        }

        userNotificationMapper.update(null,
                new LambdaUpdateWrapper<UserNotification>()
                        .eq(UserNotification::getId, notificationId)
                        .eq(UserNotification::getUserId, userId)
                        .set(UserNotification::getIsDeleted, 1));

        return Result.success("删除成功");
    }

    @Override
    public Result<String> clearAllNotifications() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        userNotificationMapper.deleteAllByUserId(userId);
        return Result.success("已清空所有通知");
    }

    @Transactional(readOnly = true)
    @Override
    public Result<NotificationStatsVO> getNotificationStats() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        Long totalCount = userNotificationMapper.countByUserId(userId);
        Long unreadCount = userNotificationMapper.countUnreadByUserId(userId);

        return Result.success(NotificationStatsVO.builder()
                .totalCount(totalCount)
                .unreadCount(unreadCount)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendFollowNotification(Long fromUserId, Long toUserId, String fromUserName) {
        if (fromUserId.equals(toUserId)) {
            return;
        }

        UserNotification notification = new UserNotification();
        notification.setUserId(toUserId);
        notification.setFromUserId(fromUserId);
        notification.setType("follow");
        notification.setTitle("新关注");
        notification.setContent("用户 " + fromUserName + " 关注了你");
        notification.setLink("/profile/" + fromUserId);
        notification.setIsRead(0);
        notification.setIsDeleted(0);
        notification.setCreateTime(new Date());

        userNotificationMapper.insert(notification);
        log.info("发送关注通知: {} -> {}", fromUserId, toUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendCommentNotification(Long fromUserId, Long toUserId, Long targetId,
                                        String targetType, String content, Long businessId) {
        if (fromUserId.equals(toUserId)) {
            return;
        }

        User fromUser = userMapper.selectById(fromUserId);
        String fromUserName = fromUser != null ? fromUser.getUserName() : "未知用户";

        UserNotification notification = new UserNotification();
        notification.setUserId(toUserId);
        notification.setFromUserId(fromUserId);
        notification.setType("comment_reply");
        notification.setTitle("新评论");
        notification.setContent("用户 " + fromUserName + " 评论了你的" +
                ("post".equals(targetType) ? "帖子" : "资源") + "：" + content);
        notification.setBusinessId(businessId);

        if ("post".equals(targetType)) {
            notification.setLink("/forum/" + businessId);
        } else {
            notification.setLink("/resource/" + businessId);
        }

        notification.setIsRead(0);
        notification.setIsDeleted(0);
        notification.setCreateTime(new Date());

        userNotificationMapper.insert(notification);
        log.info("发送评论通知: {} -> {}, targetId: {}", fromUserId, toUserId, targetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendPostFavoriteNotification(Long fromUserId, Long toUserId, String fromUserName,
                                             Long postId, String postTitle) {
        if (fromUserId.equals(toUserId)) {
            return;
        }

        UserNotification notification = new UserNotification();
        notification.setUserId(toUserId);
        notification.setFromUserId(fromUserId);
        notification.setType("post_favorite");
        notification.setTitle("帖子被收藏");
        notification.setContent("用户 " + fromUserName + " 收藏了你的帖子「" + postTitle + "」");
        notification.setBusinessId(postId);
        notification.setLink("/forum/" + postId);
        notification.setIsRead(0);
        notification.setIsDeleted(0);
        notification.setCreateTime(new Date());

        userNotificationMapper.insert(notification);
        log.info("发送帖子收藏通知: {} -> {}, postId: {}", fromUserId, toUserId, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendResourceFavoriteNotification(Long fromUserId, Long toUserId, String fromUserName,
                                                  Long resourceId, String resourceTitle) {
        if (fromUserId.equals(toUserId)) {
            return;
        }

        UserNotification notification = new UserNotification();
        notification.setUserId(toUserId);
        notification.setFromUserId(fromUserId);
        notification.setType("resource_favorite");
        notification.setTitle("资源被收藏");
        notification.setContent("用户 " + fromUserName + " 收藏了你的资源「" + resourceTitle + "」");
        notification.setBusinessId(resourceId);
        notification.setLink("/resource/" + resourceId);
        notification.setIsRead(0);
        notification.setIsDeleted(0);
        notification.setCreateTime(new Date());

        userNotificationMapper.insert(notification);
        log.info("发送资源收藏通知: {} -> {}, resourceId: {}", fromUserId, toUserId, resourceId);
    }
}
