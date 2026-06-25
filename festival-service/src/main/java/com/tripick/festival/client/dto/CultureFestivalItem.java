package com.tripick.festival.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 전국문화축제표준데이터(data.go.kr) API 응답 1건.
 * odcloud 표준데이터 API는 컬럼명을 한글 그대로 JSON 키로 내려준다 - 실제 키가 다르면 여기 @JsonProperty만 고치면 됨.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CultureFestivalItem {

    @JsonProperty("축제명")
    private String festivalName;

    @JsonProperty("축제시작일자")
    private String startDate;

    @JsonProperty("축제종료일자")
    private String endDate;

    @JsonProperty("개최장소")
    private String venue;

    @JsonProperty("축제내용")
    private String content;

    @JsonProperty("홈페이지주소")
    private String homepageUrl;

    @JsonProperty("소재지도로명주소")
    private String roadAddress;

    @JsonProperty("소재지지번주소")
    private String lotAddress;

    @JsonProperty("위도")
    private String latitude;

    @JsonProperty("경도")
    private String longitude;

    public String getAddress() {
        return (roadAddress != null && !roadAddress.isBlank()) ? roadAddress : lotAddress;
    }
}
