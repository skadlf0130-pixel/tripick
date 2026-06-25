package com.tripick.festival.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.festival.entity.Bookmark;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final FestivalService festivalService;

    @Transactional
    public void bookmark(Long userId, Long festivalId) {
        if (bookmarkRepository.existsByUserIdAndFestivalId(userId, festivalId)) {
            throw new TripickException(ErrorCode.ALREADY_BOOKMARKED);
        }
        Festival festival = festivalService.getFestival(festivalId);

        bookmarkRepository.save(Bookmark.builder()
                .userId(userId)
                .festival(festival)
                .build());
    }

    @Transactional
    public void unbookmark(Long userId, Long festivalId) {
        Bookmark bookmark = bookmarkRepository.findByUserIdAndFestivalId(userId, festivalId)
                .orElseThrow(() -> new TripickException(ErrorCode.BOOKMARK_NOT_FOUND));
        bookmarkRepository.delete(bookmark);
    }

    public Page<Bookmark> getBookmarks(Long userId, Pageable pageable) {
        return bookmarkRepository.findByUserId(userId, pageable);
    }
}
