package com.tripick.festival.dto.response;

import com.tripick.festival.entity.TravelSpot;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TravelSpotDetailResponse {

    private final Long spotId;
    private final String name;
    private final String region;
    private final String category;
    private final String description;
    private final String imageUrl;
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public TravelSpotDetailResponse(TravelSpot spot) {
        this.spotId = spot.getId();
        this.name = spot.getName();
        this.region = spot.getRegion();
        this.category = spot.getCategory();
        this.description = spot.getDescription();
        this.imageUrl = spot.getImageUrl();
        this.latitude = spot.getLatitude();
        this.longitude = spot.getLongitude();
    }
}
