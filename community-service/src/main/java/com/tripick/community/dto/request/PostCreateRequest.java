package com.tripick.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다")
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    @Size(max = 2000, message = "내용은 최대 2000자까지 입력 가능합니다")
    private String content;

    private Long festivalId;

    private List<String> photoUrls;

    private String videoUrl;
}
