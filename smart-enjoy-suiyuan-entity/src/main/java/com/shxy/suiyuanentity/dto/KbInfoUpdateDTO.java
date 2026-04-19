package com.shxy.suiyuanentity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KbInfoUpdateDTO {

    private String kbName;

    private String kbDescription;

    private String kbCategory;

    private Integer status;
}