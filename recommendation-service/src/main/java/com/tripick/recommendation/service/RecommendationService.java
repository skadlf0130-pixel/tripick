package com.tripick.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.recommendation.client.ClaudeRecommendationClient;
import com.tripick.recommendation.client.FestivalClient;
import com.tripick.recommendation.client.dto.AiFestivalPick;
import com.tripick.recommendation.client.dto.AiRecommendationResult;
import com.tripick.recommendation.client.dto.FestivalCandidate;
import com.tripick.recommendation.client.dto.TravelSpotCandidate;
import com.tripick.recommendation.dto.request.RecommendationRequest;
import com.tripick.recommendation.dto.response.RecommendationResponse;
import com.tripick.recommendation.dto.response.RecommendedFestivalResponse;
import com.tripick.recommendation.dto.response.RecommendedSpotResponse;
import com.tripick.recommendation.entity.Recommendation;
import com.tripick.recommendation.entity.RecommendationFestival;
import com.tripick.recommendation.entity.RecommendationSpot;
import com.tripick.recommendation.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int CANDIDATE_PAGE_SIZE = 20;

    private final RecommendationRepository recommendationRepository;
    private final FestivalClient festivalClient;
    private final ClaudeRecommendationClient claudeRecommendationClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public Recommendation recommend(Long userId, RecommendationRequest request) {
        String conditionKey = buildConditionKey(request);

        return recommendationRepository
                .findFirstByConditionKeyAndExpiresAtAfterOrderByIdDesc(conditionKey, LocalDateTime.now())
                .orElseGet(() -> generateRecommendation(userId, request, conditionKey));
    }

    public Recommendation getRecommendation(Long id) {
        return recommendationRepository.findById(id)
                .orElseThrow(() -> new TripickException(ErrorCode.RECOMMENDATION_NOT_FOUND));
    }

    public RecommendationResponse toResponse(Recommendation recommendation) {
        List<RecommendedFestivalResponse> festivals = recommendation.getRecommendationFestivals().stream()
                .map(rf -> new RecommendedFestivalResponse(
                        festivalClient.getFestival(rf.getFestivalId()).getData(), rf))
                .toList();

        List<RecommendedSpotResponse> spots = recommendation.getRecommendationSpots().stream()
                .map(rs -> new RecommendedSpotResponse(
                        festivalClient.getTravelSpot(rs.getSpotId()).getData(), rs))
                .toList();

        return new RecommendationResponse(
                recommendation.getId(), festivals, spots, recommendation.getCreatedAt());
    }

    private Recommendation generateRecommendation(Long userId, RecommendationRequest request, String conditionKey) {
        List<FestivalCandidate> festivalCandidates = festivalClient
                .getFestivals(request.getRegion(), 0, CANDIDATE_PAGE_SIZE).getData().getContent();
        List<TravelSpotCandidate> spotCandidates = festivalClient
                .getTravelSpots(request.getRegion(), 0, CANDIDATE_PAGE_SIZE).getData().getContent();

        AiRecommendationResult aiResult = claudeRecommendationClient.recommend(request, festivalCandidates, spotCandidates);

        Recommendation recommendation = Recommendation.builder()
                .userId(userId)
                .region(request.getRegion())
                .interests(toJson(request.getInterests()))
                .transportation(request.getTransportation())
                .conditionKey(conditionKey)
                .resultJson(toJson(aiResult))
                .expiresAt(Recommendation.defaultExpiresAt())
                .build();

        int order = 1;
        for (AiFestivalPick pick : aiResult.festivals()) {
            recommendation.getRecommendationFestivals().add(RecommendationFestival.builder()
                    .recommendation(recommendation)
                    .festivalId(pick.festivalId())
                    .aiComment(pick.aiComment())
                    .sortOrder(order++)
                    .build());
        }

        order = 1;
        for (Long spotId : aiResult.travelSpotIds()) {
            recommendation.getRecommendationSpots().add(RecommendationSpot.builder()
                    .recommendation(recommendation)
                    .spotId(spotId)
                    .sortOrder(order++)
                    .build());
        }

        return recommendationRepository.save(recommendation);
    }

    private String buildConditionKey(RecommendationRequest request) {
        String interests = request.getInterests() == null
                ? ""
                : request.getInterests().stream().sorted().collect(Collectors.joining(","));
        return String.join("|",
                Objects.toString(request.getRegion(), ""),
                interests,
                Objects.toString(request.getTransportation(), ""));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new TripickException(ErrorCode.AI_API_ERROR);
        }
    }
}
