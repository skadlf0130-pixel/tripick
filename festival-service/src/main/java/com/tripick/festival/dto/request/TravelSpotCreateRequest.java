package com.tripick.festival.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TravelSpotCreateRequest {

    @NotBlank(message = "여행지명을 입력해주세요")
    @Size(max = 200, message = "여행지명은 최대 200자까지 입력 가능합니다")
    private String name;

    private String region;

    private String category;

    private String description;

    private String imageUrl;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
