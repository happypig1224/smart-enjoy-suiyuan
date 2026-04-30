package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.constant.RedisConstant;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.entity.UserFollow;
import com.shxy.suiyuanentity.vo.UserFollowVO;
import com.shxy.suiyuanserver.mapper.UserFollowMapper;
import com.shxy.suiyuanserver.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author 33046
* @description 针对表【user_follow(用户关注关系表)】的数据库操作Service实现
* @createDate 2026-04-28 20:22:21
*/
@Slf4j
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
    implements UserFollowService{

    private final UserFollowMapper userFollowMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public UserFollowServiceImpl(UserFollowMapper userFollowMapper, StringRedisTemplate stringRedisTemplate) {
        this.userFollowMapper = userFollowMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 关注用户
     * @param followeeId 被关注者ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> followUser(Long followeeId) {
        if (followeeId == null || followeeId <= 0) {
            return Result.fail("被关注者ID不合法");
        }

        Long followerId = BaseContext.getCurrentUserId();
        if (followerId == null) {
            return Result.fail("用户未登录");
        }

        if (followerId.equals(followeeId)) {
            return Result.fail("不能关注自己");
        }

        UserFollow existingFollow = userFollowMapper.selectByFollowerAndFollowee(followerId, followeeId);
        if (existingFollow != null) {
            return Result.fail("已经关注过该用户");
        }

        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFolloweeId(followeeId);
        userFollow.setCreateTime(new Date());

        int insert = userFollowMapper.insert(userFollow);
        if (insert <= 0) {
            throw new BaseException("关注失败");
        }
        // TODO 推送关注通知给被关注者
        // 1. 将通知任务加入 ZSet 缓冲区
        // Score 设置为当前时间戳 + 5000ms (即延迟5秒处理)
        long delayScore = System.currentTimeMillis() + 5000;

        // Value包含所有必要信息
        String taskValue = String.format("{\"type\":\"follow\",\"from\":%d,\"to\":%d,\"time\":%d}",
                followerId, followeeId, System.currentTimeMillis());

        stringRedisTemplate.opsForZSet().add(RedisConstant.NOTIFY_BUFFER_KEY, taskValue, delayScore);

        log.info("用户 {} 关注了用户 {}", followerId, followeeId);
        return Result.success("关注成功");
    }

    /**
     * 取消关注
     * @param followeeId 被关注者ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> unfollowUser(Long followeeId) {
        if (followeeId == null || followeeId <= 0) {
            return Result.fail("被关注者ID不合法");
        }

        Long followerId = BaseContext.getCurrentUserId();
        if (followerId == null) {
            return Result.fail("用户未登录");
        }

        UserFollow existingFollow = userFollowMapper.selectByFollowerAndFollowee(followerId, followeeId);
        if (existingFollow == null) {
            return Result.fail("尚未关注该用户");
        }

        int delete = userFollowMapper.deleteById(existingFollow.getId());
        if (delete <= 0) {
            throw new BaseException("取消关注失败");
        }
        // 取关不需要通知
        log.info("用户 {} 取消关注了用户 {}", followerId, followeeId);
        return Result.success("取消关注成功");
    }

    /**
     * 获取关注列表
     * @return 关注列表
     */
    @Override
    public Result<List<UserFollowVO>> getFollowList() {
        Long followerId = BaseContext.getCurrentUserId();
        if (followerId == null) {
            return Result.fail("用户未登录");
        }

        List<UserFollowVO> followList = userFollowMapper.selectFollowList(followerId);
        return Result.success(followList);
    }

    /**
     * 检查是否已关注
     * @param followeeId 被关注者ID
     * @return 是否已关注
     */
    @Override
    public Result<Boolean> isFollowing(Long followeeId) {
        if (followeeId == null || followeeId <= 0) {
            return Result.fail("被关注者ID不合法");
        }

        Long followerId = BaseContext.getCurrentUserId();
        if (followerId == null) {
            return Result.fail("用户未登录");
        }

        UserFollow existingFollow = userFollowMapper.selectByFollowerAndFollowee(followerId, followeeId);
        return Result.success(existingFollow != null);
    }

    /**
     * 获取用户通知列表
     */
    @Override
    public Result<List<String>> getNotifications() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        String notifyKey = RedisConstant.NOTIFY_LIST_KEY_PREFIX + userId;
        // 获取最近 50 条通知
        List<String> notifications = stringRedisTemplate.opsForList().range(notifyKey, 0, 49);
        return Result.success(notifications != null ? notifications : java.util.Collections.emptyList());
    }

    /**
     * 清除未读通知数
     */
    @Override
    public Result<String> clearUnreadCount() {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }

        String unreadKey = RedisConstant.NOTIFY_UNREAD_KEY_PREFIX + userId;
        stringRedisTemplate.delete(unreadKey);
        return Result.success("已清除未读数");
    }
}




