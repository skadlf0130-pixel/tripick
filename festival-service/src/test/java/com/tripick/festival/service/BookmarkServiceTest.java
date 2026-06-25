package com.tripick.festival.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.festival.entity.Bookmark;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.repository.BookmarkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private FestivalService festivalService;

    @InjectMocks
    private BookmarkService bookmarkService;

    @Test
    void 찜하기_성공() {
        Long userId = 1L;
        Long festivalId = 10L;
        Festival festival = Festival.builder().apiId("test-1").name("테스트 축제").build();
        when(bookmarkRepository.existsByUserIdAndFestivalId(userId, festivalId)).thenReturn(false);
        when(festivalService.getFestival(festivalId)).thenReturn(festival);

        bookmarkService.bookmark(userId, festivalId);

        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void 이미찜한축제를_다시찜하면_예외발생() {
        Long userId = 1L;
        Long festivalId = 10L;
        when(bookmarkRepository.existsByUserIdAndFestivalId(userId, festivalId)).thenReturn(true);

        assertThatThrownBy(() -> bookmarkService.bookmark(userId, festivalId))
                .isInstanceOf(TripickException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_BOOKMARKED);

        verify(bookmarkRepository, never()).save(any());
    }

    @Test
    void 찜취소_성공() {
        Long userId = 1L;
        Long festivalId = 10L;
        Bookmark bookmark = Bookmark.builder().userId(userId).build();
        when(bookmarkRepository.findByUserIdAndFestivalId(userId, festivalId)).thenReturn(java.util.Optional.of(bookmark));

        bookmarkService.unbookmark(userId, festivalId);

        verify(bookmarkRepository).delete(bookmark);
    }

    @Test
    void 찜하지않은축제를_취소하면_예외발생() {
        Long userId = 1L;
        Long festivalId = 10L;
        when(bookmarkRepository.findByUserIdAndFestivalId(userId, festivalId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> bookmarkService.unbookmark(userId, festivalId))
                .isInstanceOf(TripickException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKMARK_NOT_FOUND);

        verify(bookmarkRepository, never()).delete(any());
    }
}
