package com.tripick.festival.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "평점을 입력해주세요")
    @Min(value = 1, message = "평점은 1~5 사이여야 합니다")
    @Max(value = 5, message = "평점은 1~5 사이여야 합니다")
    private Integer rating;

    @Size(max = 500, message = "후기는 최대 500자까지 입력 가능합니다")
    private String content;
}
