package com.shxy.smartlearningacademyentity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CommentCreateDTO {

    private Long parentId;

    private String content;

    private Integer type;

    private Long postId;

    private Long lostItemId;
}
