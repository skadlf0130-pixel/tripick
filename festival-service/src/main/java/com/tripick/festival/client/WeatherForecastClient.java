package com.tripick.festival.client;

import com.tripick.festival.client.dto.KmaApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 기상청 중기예보(중기육상예보/중기기온) 조회 클라이언트.
 * 날씨는 보조 기능이라 호출 실패는 예외로 던지지 않고 빈 결과로 흘려보냄 (호출부인 WeatherService에서 최종 흡수).
 */
@Slf4j
@Component
public class WeatherForecastClient {

    private static final DateTimeFormatter TM_FC_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final WebClient webClient;
    private final String serviceKey;

    public WeatherForecastClient(
            @Value("${weather-api.base-url}") String baseUrl,
            @Value("${weather-api.service-key}") String serviceKey
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
    }

    /** 중기육상예보(날씨/강수확률) item 목록 (regId 매칭 결과가 없거나 호출 실패 시 빈 리스트). */
    public List<Map<String, Object>> fetchLandForecast(String regId) {
        return fetch("/getMidLandFcst", regId);
    }

    /** 중기기온(최저/최고) item 목록. */
    public List<Map<String, Object>> fetchTemperatureForecast(String regId) {
        return fetch("/getMidTa", regId);
    }

    private List<Map<String, Object>> fetch(String path, String regId) {
        try {
            KmaApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 10)
                            .queryParam("dataType", "JSON")
                            .queryParam("regId", regId)
                            .queryParam("tmFc", latestAnnouncementTime())
                            .build())
                    .retrieve()
                    .bodyToMono(KmaApiResponse.class)
                    .block();

            if (response == null || response.getResponse() == null
                    || response.getResponse().getBody() == null
                    || response.getResponse().getBody().getItems() == null) {
                return List.of();
            }
            List<KmaApiResponse.Item> items = response.getResponse().getBody().getItems().getItem();
            return items == null ? List.of() : items.stream().map(KmaApiResponse.Item::getFields).toList();
        } catch (Exception e) {
            log.warn("기상청 중기예보 조회 실패 (path={}, regId={})", path, regId, e);
            return List.of();
        }
    }

    // 중기예보는 매일 06:00/18:00 두 차례 발표 - 현재 시각 기준 가장 최근 발표시각을 사용
    private String latestAnnouncementTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime latest = now.withHour(18).withMinute(0).withSecond(0).withNano(0);
        if (now.isBefore(latest)) {
            latest = now.withHour(6).withMinute(0).withSecond(0).withNano(0);
            if (now.isBefore(latest)) {
                latest = latest.minusDays(1).withHour(18);
            }
        }
        return latest.format(TM_FC_FORMAT);
    }
}
