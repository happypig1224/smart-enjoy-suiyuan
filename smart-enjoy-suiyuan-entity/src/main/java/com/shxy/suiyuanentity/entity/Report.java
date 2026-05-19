package com.shxy.suiyuanentity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName(value = "report")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterId;

    private String targetType;

    private Long targetId;

    private Integer reasonType;

    private String reasonDetail;

    private Integer status;

    private Long handlerId;

    private String handleResult;

    private Date handleTime;

    private Date createTime;
}
