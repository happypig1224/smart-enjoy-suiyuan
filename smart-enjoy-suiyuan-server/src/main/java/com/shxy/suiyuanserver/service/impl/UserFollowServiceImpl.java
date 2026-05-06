package com.shxy.suiyuanserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shxy.suiyuancommon.exception.BaseException;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuancommon.utils.BaseContext;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.entity.UserFollow;
import com.shxy.suiyuanentity.vo.UserFollowVO;
import com.shxy.suiyuanserver.mapper.UserFollowMapper;
import com.shxy.suiyuanserver.mapper.UserMapper;
import com.shxy.suiyuanserver.service.UserFollowService;
import com.shxy.suiyuanserver.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户关注关系Service实现
 */
@Slf4j
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
    implements UserFollowService{

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final UserNotificationService userNotificationService;

    public UserFollowServiceImpl(UserFollowMapper userFollowMapper, UserMapper userMapper, 
                                  UserNotificationService userNotificationService) {
        this.userFollowMapper = userFollowMapper;
        this.userMapper = userMapper;
        this.userNotificationService = userNotificationService;
    }

    /**
     * 关注用户
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

        // 获取关注者用户名
        User follower = userMapper.selectById(followerId);
        String followerName = follower != null ? follower.getUserName() : "未知用户";

        // 发送关注通知
        userNotificationService.sendFollowNotification(followerId, followeeId, followerName);

        log.info("用户 {} 关注了用户 {}", followerId, followeeId);
        return Result.success("关注成功");
    }

    /**
     * 取消关注
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
        
        log.info("用户 {} 取消关注了用户 {}", followerId, followeeId);
        return Result.success("取消关注成功");
    }

    /**
     * 获取关注列表
     */
    @Transactional(readOnly = true)
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
}




