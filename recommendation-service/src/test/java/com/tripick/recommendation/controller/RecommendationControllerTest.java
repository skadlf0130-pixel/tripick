package com.tripick.recommendation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── AI 추천 요청 (인증 필요) ──

    @Test
    @DisplayName("AI 추천 요청 성공 - 200 반환")
    @WithMockUser
    void recommend_validRequest_returns200() throws Exception {
        var request = Map.of(
                "region", "서울",
                "interests", List.of("자연", "먹거리", "문화"),
                "transportation", "대중교통"
        );

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recommendationId").exists())
                .andExpect(jsonPath("$.data.festivals").isArray())
                .andExpect(jsonPath("$.data.travelSpots").isArray());
    }

    @Test
    @DisplayName("AI 추천 요청 - 지역 없이 전국 추천 성공")
    @WithMockUser
    void recommend_noRegion_returns200() throws Exception {
        var request = Map.of(
                "interests", List.of("자연"),
                "transportation", "자가용"
        );

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("AI 추천 요청 - 관심사 7개 초과 400 반환")
    @WithMockUser
    void recommend_tooManyInterests_returns400() throws Exception {
        var request = Map.of(
                "region", "서울",
                "interests", List.of("자연", "먹거리", "문화", "역사", "액티비티", "쇼핑", "초과항목"),
                "transportation", "대중교통"
        );

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("AI 추천 요청 - 비인증 401 반환")
    void recommend_unauthenticated_returns401() throws Exception {
        var request = Map.of(
                "region", "서울",
                "interests", List.of("자연"),
                "transportation", "대중교통"
        );

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── 추천 결과 조회 (인증 필요) ──

    @Test
    @DisplayName("추천 결과 조회 성공 - 200 반환")
    @WithMockUser
    void getRecommendation_returns200() throws Exception {
        mockMvc.perform(get("/api/recommendations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recommendationId").value(1));
    }

    @Test
    @DisplayName("추천 결과 조회 - 비인증 401 반환")
    void getRecommendation_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/recommendations/1"))
                .andExpect(status().isUnauthorized());
    }
}
