package com.tripick.recommendation.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FestivalCandidate {

    private Long festivalId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String region;
    private String category;
    private String imageUrl;
}
