package com.tripick.festival.service;

import com.tripick.festival.client.RegionCodeMapper;
import com.tripick.festival.client.WeatherForecastClient;
import com.tripick.festival.dto.response.DailyWeatherResponse;
import com.tripick.festival.entity.Festival;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 기상청 중기예보는 오늘+3일 ~ 오늘+10일까지만 제공 - 축제 기간이 이 범위와 겹칠 때만 예보를 채운다.
 * 외부 API 실패/지역 매칭 실패는 축제 상세 조회 자체를 막지 않도록 빈 리스트로 흡수한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final int FORECAST_START_OFFSET = 3;
    private static final int FORECAST_END_OFFSET = 10;

    private final RegionCodeMapper regionCodeMapper;
    private final WeatherForecastClient weatherForecastClient;

    public List<DailyWeatherResponse> getForecast(Festival festival) {
        try {
            return resolveForecast(festival);
        } catch (Exception e) {
            log.warn("축제 날씨 예보 조회 실패 (festivalId={})", festival.getId(), e);
            return List.of();
        }
    }

    private List<DailyWeatherResponse> resolveForecast(Festival festival) {
        LocalDate today = LocalDate.now();
        LocalDate forecastStart = today.plusDays(FORECAST_START_OFFSET);
        LocalDate forecastEnd = today.plusDays(FORECAST_END_OFFSET);
        LocalDate rangeStart = festival.getStartDate().isAfter(forecastStart) ? festival.getStartDate() : forecastStart;
        LocalDate rangeEnd = festival.getEndDate().isBefore(forecastEnd) ? festival.getEndDate() : forecastEnd;
        if (rangeStart.isAfter(rangeEnd)) {
            return List.of();
        }

        String regId = regionCodeMapper.resolveRegId(festival.getRegion());
        if (regId == null) {
            return List.of();
        }

        Map<String, Object> landFields = mergeFields(weatherForecastClient.fetchLandForecast(regId));
        Map<String, Object> taFields = mergeFields(weatherForecastClient.fetchTemperatureForecast(regId));
        if (landFields.isEmpty() && taFields.isEmpty()) {
            return List.of();
        }

        List<DailyWeatherResponse> result = new ArrayList<>();
        for (LocalDate date = rangeStart; !date.isAfter(rangeEnd); date = date.plusDays(1)) {
            int offset = (int) ChronoUnit.DAYS.between(today, date);
            result.add(buildDaily(date, offset, landFields, taFields));
        }
        return result;
    }

    private Map<String, Object> mergeFields(List<Map<String, Object>> items) {
        return items.isEmpty() ? Map.of() : items.get(0);
    }

    private DailyWeatherResponse buildDaily(LocalDate date, int offset, Map<String, Object> landFields, Map<String, Object> taFields) {
        String weather;
        Integer rainProbability;
        if (offset <= 7) {
            weather = firstNonNull(landFields.get("wf" + offset + "Pm"), landFields.get("wf" + offset + "Am"));
            rainProbability = toInt(firstNonNull(landFields.get("rnSt" + offset + "Pm"), landFields.get("rnSt" + offset + "Am")));
        } else {
            weather = asString(landFields.get("wf" + offset));
            rainProbability = toInt(landFields.get("rnSt" + offset));
        }
        Integer minTemp = toInt(taFields.get("taMin" + offset));
        Integer maxTemp = toInt(taFields.get("taMax" + offset));
        return new DailyWeatherResponse(date, weather, minTemp, maxTemp, rainProbability);
    }

    private String firstNonNull(Object a, Object b) {
        return asString(a != null ? a : b);
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
