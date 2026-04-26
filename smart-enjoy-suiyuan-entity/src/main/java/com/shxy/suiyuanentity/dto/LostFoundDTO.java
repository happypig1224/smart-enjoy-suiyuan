package com.shxy.suiyuanentity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 寻物启事 DTO
 * @author huang qi long
 * @version 1.0
 * @School Suihua University
 * @since 2026/4/11 21:05
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LostFoundDTO {
    
    private Long id;

    @NotNull(message = "类型不能为空")
    private Integer type;

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100, message = "标题长度必须在1-100个字符之间")
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(min = 1, max = 2000, message = "描述长度必须在1-2000个字符之间")
    private String description;

    private Integer urgent;

    @Size(max = 100, message = "位置长度不能超过100个字符")
    private String location;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String phoneContact;

    @Pattern(regexp = "^$|^[a-zA-Z][a-zA-Z0-9_-]{5,19}$", message = "微信号格式不正确")
    private String wechatContact;

    private List<String> images;
}
