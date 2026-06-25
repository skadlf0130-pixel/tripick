package com.tripick.festival.client.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 기상청 중기예보(getMidLandFcst/getMidTa) 공통 응답 포맷.
 * item의 필드(wf3Am, wf3Pm, ..., taMin3, taMax3, ...)가 매우 많고 가변적이라 @JsonAnySetter로 모두 Map에 담아둠.
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KmaApiResponse {

    private Response response;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {
        private final Map<String, Object> fields = new HashMap<>();

        @JsonAnySetter
        public void set(String key, Object value) {
            fields.put(key, value);
        }
    }
}
