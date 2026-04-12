package com.shxy.smartlearningacademyentity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDTO {
    private String title;

    private String content;

    private Integer type;

    private List<String> images;
}
