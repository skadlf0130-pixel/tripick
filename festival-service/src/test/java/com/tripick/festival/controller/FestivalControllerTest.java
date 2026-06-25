package com.tripick.festival.controller;

import com.tripick.festival.entity.Festival;
import com.tripick.festival.repository.FestivalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FestivalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FestivalRepository festivalRepository;

    private Long festivalId;

    @BeforeEach
    void seedFestival() {
        Festival festival = festivalRepository.save(Festival.builder()
                .apiId("test-api-id")
                .name("서울 봄꽃 축제")
                .startDate(LocalDate.of(2026, 4, 10))
                .endDate(LocalDate.of(2026, 4, 20))
                .region("서울특별시 영등포구")
                .description("한강변에서 펼쳐지는 봄꽃 축제입니다.")
                .officialUrl("https://example.com")
                .build());
        this.festivalId = festival.getId();
    }

    // ── 축제 목록 조회 (공개) ──

    @Test
    @DisplayName("축제 목록 조회 성공 - 200 반환")
    void getFestivals_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(10));
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
                .andExpect(jsonPath("$.data.size").value(5));
    }

    // ── 축제 상세 조회 (공개) ──

    @Test
    @DisplayName("축제 상세 조회 성공 - 200 반환")
    void getFestival_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals/{id}", festivalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.festivalId").value(festivalId))
                .andExpect(jsonPath("$.data.name").value("서울 봄꽃 축제"))
                .andExpect(jsonPath("$.data.region").exists());
    }

    @Test
    @DisplayName("존재하지 않는 축제 상세 조회 - 404 반환")
    void getFestival_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/festivals/{id}", festivalId + 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FESTIVAL_NOT_FOUND"));
    }

    // ── 후기 목록 조회 (공개) ──

    @Test
    @DisplayName("축제 후기 목록 조회 성공 - 200 반환")
    void getReviews_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals/{id}/reviews", festivalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.averageRating").exists());
    }

    @Test
    @DisplayName("후기 목록 페이징 파라미터 적용 - 200 반환")
    void getReviews_withPaging_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals/{id}/reviews", festivalId)
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── 동기화 (관리자 전용) ──

    @Test
    @DisplayName("비인증 상태로 동기화 요청 - 인증 필요로 거부")
    void syncFestivals_withoutAuth_isForbidden() throws Exception {
        mockMvc.perform(post("/api/festivals/sync"))
                .andExpect(status().is4xxClientError());
    }
}
