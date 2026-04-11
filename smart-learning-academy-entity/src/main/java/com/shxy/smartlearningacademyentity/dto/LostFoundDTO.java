package com.shxy.smartlearningacademyentity.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LostFoundDTO {
    private Integer id;

    private Integer type;

    private String title;

    private String description;

    private Integer urgent;

    private String location;

    private String phoneContact;

    private String wechatContact;

    private List<String> images;
}
