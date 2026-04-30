package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.UserFollow;
import com.shxy.suiyuanentity.vo.UserFollowVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 33046
* @description 针对表【user_follow(用户关注关系表)】的数据库操作Mapper
* @createDate 2026-04-28 20:22:21
* @Entity com.shxy.suiyuanentity.entity.UserFollow
*/
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 查询用户的关注列表（包含被关注者的详细信息）
     * @param followerId 关注者ID
     * @return 关注列表
     */
    @Select("SELECT uf.id, uf.followee_id, uf.create_time, " +
            "u.user_name AS followee_user_name, u.avatar AS followee_avatar, u.phone AS followee_phone " +
            "FROM user_follow uf " +
            "LEFT JOIN user u ON uf.followee_id = u.id " +
            "WHERE uf.follower_id = #{followerId} " +
            "ORDER BY uf.create_time DESC")
    List<UserFollowVO> selectFollowList(Long followerId);

    /**
     * 查询是否已关注
     * @param followerId 关注者ID
     * @param followeeId 被关注者ID
     * @return 关注记录
     */
    @Select("SELECT * FROM user_follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    UserFollow selectByFollowerAndFollowee(Long followerId, Long followeeId);
}




