package com.tripick.recommendation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RecommendationRequest {

    private String region;  // null이면 전국

    @Size(max = 6, message = "관심사는 최대 6개까지 선택 가능합니다")
    private List<String> interests;

    private String transportation;  // 자가용, 대중교통, 무관
}