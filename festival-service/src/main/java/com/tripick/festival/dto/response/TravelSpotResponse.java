package com.tripick.festival.dto.response;

import com.tripick.festival.entity.TravelSpot;
import lombok.Getter;

@Getter
public class TravelSpotResponse {

    private final Long spotId;
    private final String name;
    private final String region;
    private final String category;
    private final String imageUrl;

    public TravelSpotResponse(TravelSpot spot) {
        this.spotId = spot.getId();
        this.name = spot.getName();
        this.region = spot.getRegion();
        this.category = spot.getCategory();
        this.imageUrl = spot.getImageUrl();
    }
}
