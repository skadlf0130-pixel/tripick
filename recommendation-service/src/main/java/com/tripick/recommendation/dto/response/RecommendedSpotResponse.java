package com.tripick.recommendation.dto.response;

import com.tripick.recommendation.client.dto.TravelSpotCandidate;
import com.tripick.recommendation.entity.RecommendationSpot;
import lombok.Getter;

@Getter
public class RecommendedSpotResponse {

    private final Long spotId;
    private final String name;
    private final String region;
    private final String category;
    private final String imageUrl;
    private final Integer sortOrder;

    public RecommendedSpotResponse(TravelSpotCandidate spot, RecommendationSpot recommendationSpot) {
        this.spotId = spot.getSpotId();
        this.name = spot.getName();
        this.region = spot.getRegion();
        this.category = spot.getCategory();
        this.imageUrl = spot.getImageUrl();
        this.sortOrder = recommendationSpot.getSortOrder();
    }
}
