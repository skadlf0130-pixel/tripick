package com.tripick.festival.dto.response;

import com.tripick.festival.entity.Bookmark;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BookmarkResponse {

    private final Long bookmarkId;
    private final FestivalResponse festival;
    private final LocalDateTime bookmarkedAt;

    public BookmarkResponse(Bookmark bookmark) {
        this.bookmarkId = bookmark.getId();
        this.festival = new FestivalResponse(bookmark.getFestival());
        this.bookmarkedAt = bookmark.getCreatedAt();
    }
}
