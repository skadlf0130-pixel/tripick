package com.tripick.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.recommendation.client.ClaudeRecommendationClient;
import com.tripick.recommendation.client.FestivalClient;
import com.tripick.recommendation.client.dto.AiFestivalPick;
import com.tripick.recommendation.client.dto.AiRecommendationResult;
import com.tripick.recommendation.client.dto.FestivalCandidate;
import com.tripick.recommendation.client.dto.FestivalCandidatePage;
import com.tripick.recommendation.client.dto.FestivalListEnvelope;
import com.tripick.recommendation.client.dto.TravelSpotCandidate;
import com.tripick.recommendation.client.dto.TravelSpotCandidatePage;
import com.tripick.recommendation.client.dto.TravelSpotListEnvelope;
import com.tripick.recommendation.dto.request.RecommendationRequest;
import com.tripick.recommendation.entity.Recommendation;
import com.tripick.recommendation.repository.RecommendationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private FestivalClient festivalClient;

    @Mock
    private ClaudeRecommendationClient claudeRecommendationClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void 캐시된_추천결과가_있으면_AI를_재호출하지않고_재사용한다() {
        Long userId = 1L;
        RecommendationRequest request = new RecommendationRequest("서울", List.of("자연"), "대중교통");
        Recommendation cached = Recommendation.builder().id(10L).build();

        when(recommendationRepository.findFirstByConditionKeyAndExpiresAtAfterOrderByIdDesc(any(), any()))
                .thenReturn(Optional.of(cached));

        Recommendation result = recommendationService.recommend(userId, request);

        assertThat(result).isSameAs(cached);
        verifyNoInteractions(claudeRecommendationClient);
        verify(recommendationRepository, never()).save(any());
    }

    @Test
    void 캐시가없으면_후보를조회하고_AI호출후_저장한다() {
        Long userId = 1L;
        RecommendationRequest request = new RecommendationRequest("서울", List.of("자연"), "대중교통");

        FestivalCandidate festival = new FestivalCandidate(
                1L, "서울 봄꽃 축제", LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 20), "서울", "자연", null);
        TravelSpotCandidate spot = new TravelSpotCandidate(1L, "경복궁", "서울", "문화", null);

        when(recommendationRepository.findFirstByConditionKeyAndExpiresAtAfterOrderByIdDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(festivalClient.getFestivals(any(), anyInt(), anyInt()))
                .thenReturn(new FestivalListEnvelope(new FestivalCandidatePage(List.of(festival))));
        when(festivalClient.getTravelSpots(any(), anyInt(), anyInt()))
                .thenReturn(new TravelSpotListEnvelope(new TravelSpotCandidatePage(List.of(spot))));
        when(claudeRecommendationClient.recommend(any(), any(), any()))
                .thenReturn(new AiRecommendationResult(
                        List.of(new AiFestivalPick(1L, "봄꽃과 함께하는 축제예요")),
                        List.of(1L)));
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Recommendation result = recommendationService.recommend(userId, request);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getRecommendationFestivals()).hasSize(1);
        assertThat(result.getRecommendationFestivals().get(0).getFestivalId()).isEqualTo(1L);
        assertThat(result.getRecommendationSpots()).hasSize(1);
        verify(claudeRecommendationClient).recommend(any(), any(), any());
    }

    @Test
    void 존재하지않는추천결과_조회시_예외발생() {
        when(recommendationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recommendationService.getRecommendation(999L))
                .isInstanceOf(TripickException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECOMMENDATION_NOT_FOUND);
    }
}
