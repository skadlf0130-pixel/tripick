package com.tripick.festival.dto.response;

import java.time.LocalDate;

public record DailyWeatherResponse(LocalDate date, String weather, Integer minTemp, Integer maxTemp, Integer rainProbability) {
}
