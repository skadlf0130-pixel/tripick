package com.tripick.recommendation.client.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record AiFestivalPick(
        @JsonPropertyDescription("추천 후보 목록에 있는 festivalId 중 하나") Long festivalId,
        @JsonPropertyDescription("이 축제를 추천하는 이유를 1~2문장의 한국어로 작성") String aiComment
) {
}
