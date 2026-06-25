package com.tripick.festival.controller;

import com.tripick.common.dto.response.ApiResponse;
import com.tripick.festival.dto.response.BookmarkResponse;
import com.tripick.festival.entity.Bookmark;
import com.tripick.festival.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bookmark", description = "축제 찜 API")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/api/festivals/{festivalId}/bookmarks")
    @Operation(summary = "축제 찜하기")
    public ResponseEntity<ApiResponse<Void>> bookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId
    ) {
        bookmarkService.bookmark(userId, festivalId);
        return ResponseEntity.ok(ApiResponse.ok("찜 목록에 추가되었습니다."));
    }

    @DeleteMapping("/api/festivals/{festivalId}/bookmarks")
    @Operation(summary = "축제 찜 취소")
    public ResponseEntity<ApiResponse<Void>> unbookmark(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId
    ) {
        bookmarkService.unbookmark(userId, festivalId);
        return ResponseEntity.ok(ApiResponse.ok("찜이 취소되었습니다."));
    }

    @GetMapping("/api/bookmarks")
    @Operation(summary = "내 찜 목록 조회")
    public ResponseEntity<ApiResponse<Page<BookmarkResponse>>> getBookmarks(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<BookmarkResponse> result = bookmarkService.getBookmarks(userId, PageRequest.of(page, size))
                .map(BookmarkResponse::new);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
