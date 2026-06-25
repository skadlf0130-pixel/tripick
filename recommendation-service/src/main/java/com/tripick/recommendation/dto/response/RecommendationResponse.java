package com.tripick.recommendation.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class RecommendationResponse {

    private final Long recommendationId;
    private final List<RecommendedFestivalResponse> festivals;
    private final List<RecommendedSpotResponse> travelSpots;
    private final LocalDateTime createdAt;

    public RecommendationResponse(
            Long recommendationId,
            List<RecommendedFestivalResponse> festivals,
            List<RecommendedSpotResponse> travelSpots,
            LocalDateTime createdAt
    ) {
        this.recommendationId = recommendationId;
        this.festivals = festivals;
        this.travelSpots = travelSpots;
        this.createdAt = createdAt;
    }
}
