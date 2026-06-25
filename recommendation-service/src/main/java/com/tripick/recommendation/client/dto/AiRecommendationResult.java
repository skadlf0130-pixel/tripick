package com.tripick.recommendation.client.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record AiRecommendationResult(
        @JsonPropertyDescription("사용자 취향에 맞는 축제 추천 목록, 추천 순서대로 정렬") List<AiFestivalPick> festivals,
        @JsonPropertyDescription("추천 후보 목록에 있는 spotId 중 사용자 취향에 맞는 여행지 ID, 추천 순서대로 정렬") List<Long> travelSpotIds
) {
}
