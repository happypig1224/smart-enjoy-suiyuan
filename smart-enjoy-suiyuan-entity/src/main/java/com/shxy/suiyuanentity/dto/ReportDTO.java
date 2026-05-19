package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportDTO {

    @NotBlank(message = "举报对象类型不能为空")
    private String targetType;

    @NotNull(message = "举报对象ID不能为空")
    private Long targetId;

    @NotNull(message = "举报原因类型不能为空")
    @Min(value = 1, message = "举报原因类型最小为1")
    @Max(value = 6, message = "举报原因类型最大为6")
    private Integer reasonType;

    private String reasonDetail;
}
