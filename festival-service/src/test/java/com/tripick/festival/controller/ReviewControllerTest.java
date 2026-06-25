package com.tripick.festival.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.entity.Review;
import com.tripick.festival.repository.FestivalRepository;
import com.tripick.festival.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Long festivalId;

    @BeforeEach
    void seedFestival() {
        Festival festival = festivalRepository.save(Festival.builder()
                .apiId("review-test-api-id")
                .name("전주 한지 축제")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 3))
                .region("전주시")
                .build());
        this.festivalId = festival.getId();
    }

    @Test
    @DisplayName("후기 목록 조회 성공 - 200 반환")
    void getReviews_returns200() throws Exception {
        mockMvc.perform(get("/api/festivals/{id}/reviews", festivalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.averageRating").exists());
    }

    @Test
    @DisplayName("비인증 상태로 후기 작성 - 인증 필요로 거부")
    void createReview_withoutAuth_isForbidden() throws Exception {
        mockMvc.perform(post("/api/festivals/{id}/reviews", festivalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 5, "content", "좋았어요"))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("후기 작성 성공 - 200 반환")
    void createReview_returns200() throws Exception {
        mockMvc.perform(post("/api/festivals/{id}/reviews", festivalId)
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 5, "content", "좋았어요"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    @DisplayName("평점 범위를 벗어난 후기 작성 - 400 반환")
    void createReview_invalidRating_returns400() throws Exception {
        mockMvc.perform(post("/api/festivals/{id}/reviews", festivalId)
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 7, "content", "좋았어요"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("같은 축제에 후기를 두 번 작성하면 409 반환")
    void createReview_duplicate_returns409() throws Exception {
        reviewRepository.save(Review.builder()
                .userId(1L)
                .festival(festivalRepository.findById(festivalId).orElseThrow())
                .rating(4)
                .content("기존 후기")
                .build());

        mockMvc.perform(post("/api/festivals/{id}/reviews", festivalId)
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 5, "content", "또 작성"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ALREADY_REVIEWED"));
    }

    @Test
    @DisplayName("본인 후기 수정 성공 - 200 반환")
    void updateReview_returns200() throws Exception {
        Review review = reviewRepository.save(Review.builder()
                .userId(1L)
                .festival(festivalRepository.findById(festivalId).orElseThrow())
                .rating(4)
                .content("기존 후기")
                .build());

        mockMvc.perform(patch("/api/festivals/{festivalId}/reviews/{reviewId}", festivalId, review.getId())
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 2, "content", "수정된 후기"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(2))
                .andExpect(jsonPath("$.data.content").value("수정된 후기"));
    }

    @Test
    @DisplayName("타인 후기 수정 시도 - 403 반환")
    void updateReview_byOtherUser_returns403() throws Exception {
        Review review = reviewRepository.save(Review.builder()
                .userId(1L)
                .festival(festivalRepository.findById(festivalId).orElseThrow())
                .rating(4)
                .content("기존 후기")
                .build());

        mockMvc.perform(patch("/api/festivals/{festivalId}/reviews/{reviewId}", festivalId, review.getId())
                        .header("X-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rating", 2, "content", "수정 시도"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("REVIEW_WRITER_MISMATCH"));
    }

    @Test
    @DisplayName("본인 후기 삭제 성공 - 200 반환")
    void deleteReview_returns200() throws Exception {
        Review review = reviewRepository.save(Review.builder()
                .userId(1L)
                .festival(festivalRepository.findById(festivalId).orElseThrow())
                .rating(4)
                .content("기존 후기")
                .build());

        mockMvc.perform(delete("/api/festivals/{festivalId}/reviews/{reviewId}", festivalId, review.getId())
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("존재하지 않는 후기 삭제 - 404 반환")
    void deleteReview_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/festivals/{festivalId}/reviews/{reviewId}", festivalId, 999999L)
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("REVIEW_NOT_FOUND"));
    }
}
