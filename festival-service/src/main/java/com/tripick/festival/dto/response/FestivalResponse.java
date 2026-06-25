package com.tripick.festival.dto.response;

import com.tripick.festival.entity.Festival;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class FestivalResponse {

    private final Long festivalId;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String region;
    private final String category;
    private final String imageUrl;

    public FestivalResponse(Festival festival) {
        this.festivalId = festival.getId();
        this.name = festival.getName();
        this.startDate = festival.getStartDate();
        this.endDate = festival.getEndDate();
        this.region = festival.getRegion();
        this.category = festival.getCategory();
        this.imageUrl = festival.getImageUrl();
    }
}
