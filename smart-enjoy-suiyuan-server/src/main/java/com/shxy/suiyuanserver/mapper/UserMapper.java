package com.shxy.suiyuanserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shxy.suiyuanentity.entity.User;
import com.shxy.suiyuanentity.vo.AuthorStatsVO;
import com.shxy.suiyuanentity.vo.UserVO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* @author Wu, Hui Ming
* @description 针对表【user】的数据库操作Mapper
* @createDate 2026-04-04 21:30:08
* @Entity com.shxy.suiyuanentity.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    @Update("update user set user_password = #{newPassword} where id = #{userId}")
    int updatePassword(Long userId, String newPassword);

    int updateUserInfo(User user);

    @Select("select id, user_name, avatar, phone, create_time, update_time, role, status from user where id = #{userId}")
    User getUserInfo(Long userId);

    @Update("update user set avatar = #{avatarUrl} where id = #{userId}")
    void updateAvatar(Long userId, String avatarUrl);

    @Update("update user set phone = #{newPhone} where id = #{userId}")
    int updatePhone(Long userId, String newPhone);

    @Select("select * from user where user_name = #{userName}")
    User getUserByName(String userName);

    /**
     * 查询作者统计信息（文章数和粉丝数）
     */
    AuthorStatsVO selectAuthorStats(Long userId);

    /**
     * 锁定用户
     */
    @Update("update user set status = 0 where phone = #{phone}")
    void lockUser(String phone);
}




