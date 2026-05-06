package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.UserNotification;
import com.shxy.suiyuanentity.vo.NotificationVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Wu, Hui Ming
 * @description 针对表【user_notification(用户通知表)】的数据库操作Mapper
 * @createDate 2026-05-04 16:54:21
 * @Entity com.shxy.suiyuanentity.entity.UserNotification
 */
public interface UserNotificationMapper extends BaseMapper<UserNotification> {

    /**
     * 查询用户的通知列表
     */
    @Select("SELECT n.id, n.type, n.title, n.content, n.from_user_id AS userId, " +
            "n.business_id AS businessId, n.link, n.is_read AS isRead, n.create_time AS createTime, " +
            "u.user_name AS userName, u.avatar AS userAvatar " +
            "FROM user_notification n " +
            "LEFT JOIN user u ON n.from_user_id = u.id " +
            "WHERE n.user_id = #{userId} AND n.is_deleted = 0 " +
            "ORDER BY n.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<NotificationVO> selectNotificationList(Long userId, int offset, int limit);

    /**
     * 统计用户的通知总数
     */
    @Select("SELECT COUNT(*) FROM user_notification WHERE user_id = #{userId} AND is_deleted = 0")
    Long countByUserId(Long userId);

    /**
     * 统计用户的未读通知数
     */
    @Select("SELECT COUNT(*) FROM user_notification WHERE user_id = #{userId} AND is_deleted = 0 AND is_read = 0")
    Long countUnreadByUserId(Long userId);

    /**
     * 统计用户指定类型的未读通知数
     */
    @Select("SELECT COUNT(*) FROM user_notification WHERE user_id = #{userId} AND type = #{type} AND is_deleted = 0 AND is_read = 0")
    Long countUnreadByType(Long userId, String type);

    /**
     * 删除用户的所有通知
     */
    @Select("UPDATE user_notification SET is_deleted = 1 WHERE user_id = #{userId}")
    int deleteAllByUserId(Long userId);
}
