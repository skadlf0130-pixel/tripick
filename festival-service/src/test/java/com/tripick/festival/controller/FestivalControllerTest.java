package com.tripick.festival.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FestivalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ── 축제 목록 조회 (공개) ──

    @Test
    @DisplayName("축제 목록 조회 성공 - 200 반환")
    void getFestivals_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    @DisplayName("지역/월 필터 적용 축제 목록 조회 - 200 반환")
    void getFestivals_withFilters_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals")
                        .param("region", "서울")
                        .param("month", "4")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pageSize").value(5));
    }

    // ── 축제 상세 조회 (공개) ──

    @Test
    @DisplayName("축제 상세 조회 성공 - 200 반환")
    void getFestival_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.festivalId").value(1))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.region").exists());
    }

    // ── 후기 목록 조회 (공개) ──

    @Test
    @DisplayName("축제 후기 목록 조회 성공 - 200 반환")
    void getReviews_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.averageRating").exists());
    }

    @Test
    @DisplayName("후기 목록 페이징 파라미터 적용 - 200 반환")
    void getReviews_withPaging_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals/1/reviews")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}