package com.tripick.recommendation.dto.response;

import com.tripick.recommendation.client.dto.FestivalCandidate;
import com.tripick.recommendation.entity.RecommendationFestival;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RecommendedFestivalResponse {

    private final Long festivalId;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String region;
    private final String category;
    private final String imageUrl;
    private final String aiComment;
    private final Integer sortOrder;

    public RecommendedFestivalResponse(FestivalCandidate festival, RecommendationFestival recommendationFestival) {
        this.festivalId = festival.getFestivalId();
        this.name = festival.getName();
        this.startDate = festival.getStartDate();
        this.endDate = festival.getEndDate();
        this.region = festival.getRegion();
        this.category = festival.getCategory();
        this.imageUrl = festival.getImageUrl();
        this.aiComment = recommendationFestival.getAiComment();
        this.sortOrder = recommendationFestival.getSortOrder();
    }
}
