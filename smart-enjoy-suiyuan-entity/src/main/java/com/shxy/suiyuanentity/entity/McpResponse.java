package com.shxy.suiyuanentity.entity;

import lombok.Data;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/27 17:33
 */
@Data
public class McpResponse {
    /**
     * 状态码 (200为成功)
     */
    private Integer code;

    /**
     * Agent 返回的最终文本内容
     */
    private String result;

    /**
     * 提示信息
     */
    private String message;
}