package com.shxy.smartlearningacademyentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息表
 * @TableName user
 */
@TableName(value ="user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 登录用户名，全局唯一
     */
    private String userName;

    /**
     * 登录密码，MD5加密存储
     */
    private String userPassword;

    /**
     * 性别: 0-未填写, 1-男, 2-女
     */
    private Integer userGender;

    /**
     * 用户年龄
     */
    private Integer userAge;

    /**
     * 所属年级，如2023级
     */
    private String userGrade;

    /**
     * 头像图片URL地址
     */
    private String avatar;

    /**
     * 绑定手机号，唯一索引
     */
    private String phone;

    /**
     * 角色权限: 1-普通用户, 0-管理员
     */
    private Integer role;

    /**
     * 账户状态: 1-正常, 0-禁用(封号)
     */
    private Integer status;

    /**
     * 注册/创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    private Integer isDeleted;
}