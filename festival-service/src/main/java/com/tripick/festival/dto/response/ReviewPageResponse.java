package com.tripick.festival.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class ReviewPageResponse {

    private final List<ReviewResponse> content;
    private final double averageRating;
    private final int totalPages;
    private final long totalElements;

    public ReviewPageResponse(Page<ReviewResponse> page, double averageRating) {
        this.content = page.getContent();
        this.averageRating = averageRating;
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }
}
