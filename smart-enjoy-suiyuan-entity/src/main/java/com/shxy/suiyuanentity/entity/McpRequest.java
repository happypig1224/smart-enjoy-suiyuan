package com.shxy.suiyuanentity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Wu, Hui Ming   
 * @version 1.0
 * @since 2026/4/27 17:30 
 * @School Suihua University
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpRequest {
    /**
     * 要调用的工具名称，例如 "chat_agent"
     */
    private String tool;

    /**
     * 传递给 Python 的参数字典
     */
    private Map<String, Object> params;
}