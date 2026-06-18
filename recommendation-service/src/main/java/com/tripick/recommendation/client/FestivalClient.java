package com.tripick.recommendation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * [recommendation-service] festival-service 동기 호출 Feign 클라이언트
 * AI 추천 생성 시 축제 상세 정보 조회에 사용 (즉시 응답이 필요한 동기 통신)
 * 이벤트성 후처리는 Kafka 사용, 데이터 조회는 Feign 사용 원칙
 */
@FeignClient(name = "festival-service", url = "${feign.festival-service.url:http://localhost:8082}")
public interface FestivalClient {

    @GetMapping("/api/festivals/{id}")
    Map<String, Object> getFestival(@PathVariable Long id);
}
