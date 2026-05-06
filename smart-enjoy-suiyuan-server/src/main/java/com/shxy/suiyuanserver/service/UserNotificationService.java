package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.PageResult;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.UserNotification;
import com.shxy.suiyuanentity.vo.NotificationStatsVO;
import com.shxy.suiyuanentity.vo.NotificationVO;


/**
 * @author 33046
 * @description 针对表【user_notification(用户通知表)】的数据库操作Service
 * @createDate 2026-05-04 16:54:21
 */
public interface UserNotificationService extends IService<UserNotification> {

    /**
     * 获取用户通知列表
     */
    Result<PageResult> getNotifications(Integer page, Integer size);

    /**
     * 标记通知为已读
     */
    Result<String> markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    Result<String> markAllAsRead();

    /**
     * 删除通知
     */
    Result<String> deleteNotification(Long notificationId);

    /**
     * 清空所有通知
     */
    Result<String> clearAllNotifications();

    /**
     * 获取通知统计
     */
    Result<NotificationStatsVO> getNotificationStats();

    /**
     * 发送关注通知
     */
    void sendFollowNotification(Long fromUserId, Long toUserId, String fromUserName);

    /**
     * 发送评论通知
     */
    void sendCommentNotification(Long fromUserId, Long toUserId, Long targetId, 
                                 String targetType, String content, Long businessId);

    /**
     * 发送收藏通知
     */
    void sendPostFavoriteNotification(Long fromUserId, Long toUserId, String fromUserName, 
                                      Long postId, String postTitle);

    /**
     * 发送收藏通知
     */
    void sendResourceFavoriteNotification(Long fromUserId, Long toUserId, String fromUserName, 
                                          Long resourceId, String resourceTitle);
}
