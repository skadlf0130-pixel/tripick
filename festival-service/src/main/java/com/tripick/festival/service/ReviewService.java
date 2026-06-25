package com.tripick.festival.service;

import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.festival.dto.request.ReviewRequest;
import com.tripick.festival.entity.Festival;
import com.tripick.festival.entity.Review;
import com.tripick.festival.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FestivalService festivalService;

    @Transactional
    public Review create(Long festivalId, Long userId, ReviewRequest request) {
        if (reviewRepository.existsByFestivalIdAndUserIdAndIsDeletedFalse(festivalId, userId)) {
            throw new TripickException(ErrorCode.ALREADY_REVIEWED);
        }
        Festival festival = festivalService.getFestival(festivalId);

        return reviewRepository.save(Review.builder()
                .festival(festival)
                .userId(userId)
                .rating(request.getRating())
                .content(request.getContent())
                .build());
    }

    public Page<Review> getReviews(Long festivalId, Pageable pageable) {
        return reviewRepository.findByFestivalIdAndIsDeletedFalse(festivalId, pageable);
    }

    public double getAverageRating(Long festivalId) {
        return reviewRepository.getAverageRating(festivalId);
    }

    @Transactional
    public Review update(Long festivalId, Long reviewId, Long userId, ReviewRequest request) {
        Review review = getOwnedReview(festivalId, reviewId, userId);
        review.update(request.getRating(), request.getContent());
        return review;
    }

    @Transactional
    public void delete(Long festivalId, Long reviewId, Long userId) {
        Review review = getOwnedReview(festivalId, reviewId, userId);
        review.delete();
    }

    private Review getOwnedReview(Long festivalId, Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndFestivalId(reviewId, festivalId)
                .orElseThrow(() -> new TripickException(ErrorCode.REVIEW_NOT_FOUND));
        if (!review.isWriter(userId)) {
            throw new TripickException(ErrorCode.REVIEW_WRITER_MISMATCH);
        }
        return review;
    }
}
