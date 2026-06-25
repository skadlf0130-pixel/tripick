package com.tripick.festival.client;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.festival.client.dto.CultureFestivalApiResponse;
import com.tripick.festival.client.dto.CultureFestivalItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 전국문화축제표준데이터(data.go.kr) 조회 클라이언트.
 * service-key는 포털에서 받은 "Decoding" 키를 그대로 써야 함 - "Encoding" 키를 쓰면 WebClient가 한 번 더 인코딩해 인증 실패함.
 */
@Slf4j
@Component
public class CultureFestivalClient {

    private static final int PAGE_SIZE = 100;

    private final WebClient webClient;
    private final String serviceKey;

    public CultureFestivalClient(
            @Value("${culture-festival-api.base-url}") String baseUrl,
            @Value("${culture-festival-api.service-key}") String serviceKey
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
    }

    public List<CultureFestivalItem> fetchAll() {
        List<CultureFestivalItem> result = new ArrayList<>();
        int page = 1;
        while (true) {
            CultureFestivalApiResponse response = fetchPage(page);
            if (response.getData() == null || response.getData().isEmpty()) {
                break;
            }
            result.addAll(response.getData());
            if (response.getCurrentCount() < PAGE_SIZE || result.size() >= response.getTotalCount()) {
                break;
            }
            page++;
        }
        return result;
    }

    private CultureFestivalApiResponse fetchPage(int page) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("page", page)
                            .queryParam("perPage", PAGE_SIZE)
                            .queryParam("returnType", "JSON")
                            .queryParam("serviceKey", serviceKey)
                            .build())
                    .retrieve()
                    .bodyToMono(CultureFestivalApiResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("전국문화축제표준데이터 조회 실패 (page={})", page, e);
            throw new TripickException(ErrorCode.TOUR_API_ERROR);
        }
    }
}
