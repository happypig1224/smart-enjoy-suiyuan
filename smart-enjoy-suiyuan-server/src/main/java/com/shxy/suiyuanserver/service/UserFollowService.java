package com.shxy.suiyuanserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shxy.suiyuancommon.result.Result;
import com.shxy.suiyuanentity.entity.UserFollow;
import com.shxy.suiyuanentity.vo.UserFollowVO;

import java.util.List;
import java.util.Map;

/**
* @author 33046
* @description 针对表【user_follow(用户关注关系表)】的数据库操作Service
* @createDate 2026-04-28 20:22:21
*/
public interface UserFollowService extends IService<UserFollow> {

    /**
     * 关注用户
     * @param followeeId 被关注者ID
     * @return 结果
     */
    Result<String> followUser(Long followeeId);

    /**
     * 取消关注
     * @param followeeId 被关注者ID
     * @return 结果
     */
    Result<String> unfollowUser(Long followeeId);

    /**
     * 获取关注列表
     * @return 关注列表
     */
    Result<List<UserFollowVO>> getFollowList();

    /**
     * 检查是否已关注
     * @param followeeId 被关注者ID
     * @return 是否已关注
     */
    Result<Boolean> isFollowing(Long followeeId);

}
