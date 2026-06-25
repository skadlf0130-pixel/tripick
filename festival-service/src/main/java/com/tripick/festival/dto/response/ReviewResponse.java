package com.tripick.festival.dto.response;

import com.tripick.festival.entity.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {

    private final Long reviewId;
    private final Long festivalId;
    private final Long userId;
    private final Integer rating;
    private final String content;
    private final LocalDateTime createdAt;

    public ReviewResponse(Review review) {
        this.reviewId = review.getId();
        this.festivalId = review.getFestival().getId();
        this.userId = review.getUserId();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
    }
}
