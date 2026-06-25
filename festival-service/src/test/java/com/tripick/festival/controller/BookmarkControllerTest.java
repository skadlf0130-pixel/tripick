package com.tripick.festival.controller;

import com.tripick.festival.entity.Bookmark;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.repository.BookmarkRepository;
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
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    private Long festivalId;

    @BeforeEach
    void seedFestival() {
        Festival festival = festivalRepository.save(Festival.builder()
                .apiId("bookmark-test-api-id")
                .name("부산 불꽃 축제")
                .startDate(LocalDate.of(2026, 5, 1))
                .endDate(LocalDate.of(2026, 5, 3))
                .region("부산광역시")
                .build());
        this.festivalId = festival.getId();
    }

    @Test
    @DisplayName("비인증 상태로 찜하기 요청 - 인증 필요로 거부")
    void bookmark_withoutAuth_isForbidden() throws Exception {
        mockMvc.perform(post("/api/festivals/{id}/bookmarks", festivalId))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("찜하기 성공 - 200 반환")
    void bookmark_returns200() throws Exception {
        mockMvc.perform(post("/api/festivals/{id}/bookmarks", festivalId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이미 찜한 축제를 다시 찜하면 409 반환")
    void bookmark_alreadyBookmarked_returns409() throws Exception {
        bookmarkRepository.save(Bookmark.builder()
                .userId(1L)
                .festival(festivalRepository.findById(festivalId).orElseThrow())
                .build());

        mockMvc.perform(post("/api/festivals/{id}/bookmarks", festivalId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ALREADY_BOOKMARKED"));
    }

    @Test
    @DisplayName("찜 취소 성공 - 200 반환")
    void unbookmark_returns200() throws Exception {
        bookmarkRepository.save(Bookmark.builder()
                .userId(1L)
                .festival(festivalRepository.findById(festivalId).orElseThrow())
                .build());

        mockMvc.perform(delete("/api/festivals/{id}/bookmarks", festivalId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("찜하지 않은 축제를 취소하면 404 반환")
    void unbookmark_notBookmarked_returns404() throws Exception {
        mockMvc.perform(delete("/api/festivals/{id}/bookmarks", festivalId)
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("BOOKMARK_NOT_FOUND"));
    }

    @Test
    @DisplayName("내 찜 목록 조회 성공 - 200 반환")
    void getBookmarks_returns200() throws Exception {
        bookmarkRepository.save(Bookmark.builder()
                .userId(1L)
                .festival(festivalRepository.findById(festivalId).orElseThrow())
                .build());

        mockMvc.perform(get("/api/bookmarks")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].festival.festivalId").value(festivalId));
    }
}
