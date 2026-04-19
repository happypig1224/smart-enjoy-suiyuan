package com.shxy.suiyuancommon.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/4 22:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResult implements Serializable {
    private long total; // 总记录数
    private List records; // 当前页数据集合
    private long page;
    private long size;
}
