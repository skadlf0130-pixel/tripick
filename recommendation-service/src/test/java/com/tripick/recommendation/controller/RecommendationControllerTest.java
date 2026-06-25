package com.tripick.recommendation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.recommendation.client.ClaudeRecommendationClient;
import com.tripick.recommendation.client.FestivalClient;
import com.tripick.recommendation.client.dto.AiFestivalPick;
import com.tripick.recommendation.client.dto.AiRecommendationResult;
import com.tripick.recommendation.client.dto.FestivalCandidate;
import com.tripick.recommendation.client.dto.FestivalCandidatePage;
import com.tripick.recommendation.client.dto.FestivalDetailEnvelope;
import com.tripick.recommendation.client.dto.FestivalListEnvelope;
import com.tripick.recommendation.client.dto.TravelSpotCandidate;
import com.tripick.recommendation.client.dto.TravelSpotCandidatePage;
import com.tripick.recommendation.client.dto.TravelSpotDetailEnvelope;
import com.tripick.recommendation.client.dto.TravelSpotListEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FestivalClient festivalClient;

    @MockBean
    private ClaudeRecommendationClient claudeRecommendationClient;

    @BeforeEach
    void setUp() {
        FestivalCandidate festival = new FestivalCandidate(
                1L, "서울 봄꽃 축제", LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 20), "서울", "자연", "https://example.com/image.jpg");
        TravelSpotCandidate spot = new TravelSpotCandidate(1L, "경복궁", "서울", "문화", "https://example.com/spot.jpg");

        when(festivalClient.getFestivals(any(), anyInt(), anyInt()))
                .thenReturn(new FestivalListEnvelope(new FestivalCandidatePage(List.of(festival))));
        when(festivalClient.getTravelSpots(any(), anyInt(), anyInt()))
                .thenReturn(new TravelSpotListEnvelope(new TravelSpotCandidatePage(List.of(spot))));
        when(festivalClient.getFestival(anyLong())).thenReturn(new FestivalDetailEnvelope(festival));
        when(festivalClient.getTravelSpot(anyLong())).thenReturn(new TravelSpotDetailEnvelope(spot));

        when(claudeRecommendationClient.recommend(any(), any(), any()))
                .thenReturn(new AiRecommendationResult(
                        List.of(new AiFestivalPick(1L, "대중교통 10분 거리! 봄꽃과 먹거리가 함께해요")),
                        List.of(1L)));
    }

    // ── AI 추천 요청 (인증 필요) ──

    @Test
    @DisplayName("AI 추천 요청 성공 - 200 반환")
    void recommend_validRequest_returns200() throws Exception {
        var request = Map.of(
                "region", "서울",
                "interests", List.of("자연", "먹거리", "문화"),
                "transportation", "대중교통"
        );

        mockMvc.perform(post("/api/recommendations")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recommendationId").exists())
                .andExpect(jsonPath("$.data.festivals").isArray())
                .andExpect(jsonPath("$.data.festivals[0].name").value("서울 봄꽃 축제"))
                .andExpect(jsonPath("$.data.travelSpots").isArray());
    }

    @Test
    @DisplayName("AI 추천 요청 - 지역 없이 전국 추천 성공")
    void recommend_noRegion_returns200() throws Exception {
        var request = Map.of(
                "interests", List.of("자연"),
                "transportation", "자가용"
        );

        mockMvc.perform(post("/api/recommendations")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("AI 추천 요청 - 관심사 7개 초과 400 반환")
    void recommend_tooManyInterests_returns400() throws Exception {
        var request = Map.of(
                "region", "서울",
                "interests", List.of("자연", "먹거리", "문화", "역사", "액티비티", "쇼핑", "초과항목"),
                "transportation", "대중교통"
        );

        mockMvc.perform(post("/api/recommendations")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("AI 추천 요청 - 비인증 상태로 요청 거부")
    void recommend_unauthenticated_isForbidden() throws Exception {
        var request = Map.of(
                "region", "서울",
                "interests", List.of("자연"),
                "transportation", "대중교통"
        );

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    // ── 추천 결과 조회 (인증 필요) ──

    @Test
    @DisplayName("추천 결과 조회 성공 - 200 반환")
    void getRecommendation_returns200() throws Exception {
        Long recommendationId = createRecommendation();

        mockMvc.perform(get("/api/recommendations/{id}", recommendationId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recommendationId").value(recommendationId));
    }

    @Test
    @DisplayName("존재하지 않는 추천 결과 조회 - 404 반환")
    void getRecommendation_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/recommendations/{id}", 999999L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RECOMMENDATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("추천 결과 조회 - 비인증 상태로 요청 거부")
    void getRecommendation_unauthenticated_isForbidden() throws Exception {
        mockMvc.perform(get("/api/recommendations/1"))
                .andExpect(status().is4xxClientError());
    }

    private Long createRecommendation() throws Exception {
        var request = Map.of(
                "region", "서울",
                "interests", List.of("자연"),
                "transportation", "대중교통"
        );

        String response = mockMvc.perform(post("/api/recommendations")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("data").get("recommendationId").asLong();
    }
}
