package com.tripick.festival.dto.response;

import com.tripick.festival.entity.Festival;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class FestivalDetailResponse {

    private final Long festivalId;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String region;
    private final String category;
    private final String description;
    private final String imageUrl;
    private final String officialUrl;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final List<DailyWeatherResponse> weatherForecast;

    public FestivalDetailResponse(Festival festival, List<DailyWeatherResponse> weatherForecast) {
        this.festivalId = festival.getId();
        this.name = festival.getName();
        this.startDate = festival.getStartDate();
        this.endDate = festival.getEndDate();
        this.region = festival.getRegion();
        this.category = festival.getCategory();
        this.description = festival.getDescription();
        this.imageUrl = festival.getImageUrl();
        this.officialUrl = festival.getOfficialUrl();
        this.latitude = festival.getLatitude();
        this.longitude = festival.getLongitude();
        this.weatherForecast = weatherForecast;
    }
}
