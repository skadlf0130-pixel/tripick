package com.tripick.festival.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.festival.dto.request.ReviewRequest;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.entity.Review;
import com.tripick.festival.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private FestivalService festivalService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void 후기작성_성공() {
        Long userId = 1L;
        Long festivalId = 10L;
        Festival festival = Festival.builder().apiId("test-1").name("테스트 축제").build();
        when(reviewRepository.existsByFestivalIdAndUserIdAndIsDeletedFalse(festivalId, userId)).thenReturn(false);
        when(festivalService.getFestival(festivalId)).thenReturn(festival);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review review = reviewService.create(festivalId, userId, new ReviewRequest(5, "최고의 축제였어요"));

        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("최고의 축제였어요");
    }

    @Test
    void 이미작성한축제에_다시작성하면_예외발생() {
        Long userId = 1L;
        Long festivalId = 10L;
        when(reviewRepository.existsByFestivalIdAndUserIdAndIsDeletedFalse(festivalId, userId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(festivalId, userId, new ReviewRequest(5, "내용")))
                .isInstanceOf(TripickException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_REVIEWED);

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void 본인후기_수정_성공() {
        Long userId = 1L;
        Long festivalId = 10L;
        Review review = Review.builder().userId(userId).rating(5).content("원본 후기").build();
        when(reviewRepository.findByIdAndFestivalId(1L, festivalId)).thenReturn(Optional.of(review));

        Review updated = reviewService.update(festivalId, 1L, userId, new ReviewRequest(3, "수정된 후기"));

        assertThat(updated.getRating()).isEqualTo(3);
        assertThat(updated.getContent()).isEqualTo("수정된 후기");
    }

    @Test
    void 타인후기_수정시_예외발생() {
        Long writerId = 1L;
        Long festivalId = 10L;
        Review review = Review.builder().userId(writerId).rating(5).content("원본 후기").build();
        when(reviewRepository.findByIdAndFestivalId(1L, festivalId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.update(festivalId, 1L, 999L, new ReviewRequest(3, "수정")))
                .isInstanceOf(TripickException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_WRITER_MISMATCH);
    }

    @Test
    void 본인후기_삭제_성공() {
        Long userId = 1L;
        Long festivalId = 10L;
        Review review = Review.builder().userId(userId).rating(5).content("원본 후기").build();
        when(reviewRepository.findByIdAndFestivalId(1L, festivalId)).thenReturn(Optional.of(review));

        reviewService.delete(festivalId, 1L, userId);

        assertThat(review.isDeleted()).isTrue();
    }

    @Test
    void 존재하지않는후기_수정시_예외발생() {
        when(reviewRepository.findByIdAndFestivalId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.update(10L, 1L, 1L, new ReviewRequest(3, "수정")))
                .isInstanceOf(TripickException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
    }
}
