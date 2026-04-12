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
    

    private String content;

    private Long postId;

    private Long lostItemId;
    
    private Long parentId;
}
