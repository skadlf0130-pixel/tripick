package com.tripick.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.festival.entity.TravelSpot;
import com.tripick.festival.repository.TravelSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TravelSpotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TravelSpotRepository travelSpotRepository;

    private Long spotId;

    @BeforeEach
    void seedTravelSpot() {
        TravelSpot spot = travelSpotRepository.save(TravelSpot.builder()
                .name("해운대 해수욕장")
                .region("부산광역시")
                .category("자연")
                .description("부산의 대표 해수욕장")
                .build());
        this.spotId = spot.getId();
    }

    @Test
    @DisplayName("여행지 목록 조회 성공 - 200 반환")
    void getTravelSpots_returns200() throws Exception {
        mockMvc.perform(get("/api/travel-spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("여행지 상세 조회 성공 - 200 반환")
    void getTravelSpot_returns200() throws Exception {
        mockMvc.perform(get("/api/travel-spots/{id}", spotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("해운대 해수욕장"));
    }

    @Test
    @DisplayName("존재하지 않는 여행지 상세 조회 - 404 반환")
    void getTravelSpot_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/travel-spots/{id}", spotId + 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TRAVEL_SPOT_NOT_FOUND"));
    }

    @Test
    @DisplayName("비인증 상태로 여행지 등록 - 인증 필요로 거부")
    void createTravelSpot_withoutAuth_isForbidden() throws Exception {
        mockMvc.perform(post("/api/travel-spots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "남산타워"))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("일반 사용자가 여행지 등록 시도 - 403 반환")
    void createTravelSpot_withUserRole_isForbidden() throws Exception {
        mockMvc.perform(post("/api/travel-spots")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "남산타워"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("관리자가 여행지 등록 성공 - 200 반환")
    void createTravelSpot_withAdminRole_returns200() throws Exception {
        mockMvc.perform(post("/api/travel-spots")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "남산타워", "region", "서울특별시"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("남산타워"));
    }
}
