package com.tripick.festival.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * odcloud 표준데이터 API 공통 응답 포맷({currentCount, data, matchCount, page, perPage, totalCount}).
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CultureFestivalApiResponse {

    private int currentCount;
    private List<CultureFestivalItem> data;
    private int matchCount;
    private int page;
    private int perPage;
    private int totalCount;
}
