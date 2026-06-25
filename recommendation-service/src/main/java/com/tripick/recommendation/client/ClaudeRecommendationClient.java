package com.tripick.recommendation.client;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.tripick.common.exception.ErrorCode;
import com.tripick.common.exception.TripickException;
import com.tripick.recommendation.client.dto.AiRecommendationResult;
import com.tripick.recommendation.client.dto.FestivalCandidate;
import com.tripick.recommendation.client.dto.TravelSpotCandidate;
import com.tripick.recommendation.dto.request.RecommendationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * [recommendation-service] Claude API(공식 Java SDK) 호출 클라이언트
 * festival-service에서 받은 후보 목록 중 사용자 취향에 맞는 축제/여행지를 선택하고
 * 추천 이유(aiComment)를 생성하도록 요청. output_config(구조화 출력)로 JSON 형식을 보장받는다.
 */
@Component
@RequiredArgsConstructor
public class ClaudeRecommendationClient {

    private final AnthropicClient anthropicClient;

    @Value("${ai-api.model}")
    private String model;

    public AiRecommendationResult recommend(
            RecommendationRequest request,
            List<FestivalCandidate> festivalCandidates,
            List<TravelSpotCandidate> spotCandidates
    ) {
        String prompt = buildPrompt(request, festivalCandidates, spotCandidates);

        try {
            StructuredMessageCreateParams<AiRecommendationResult> params = MessageCreateParams.builder()
                    .model(model)
                    .maxTokens(2048L)
                    .outputConfig(AiRecommendationResult.class)
                    .addUserMessage(prompt)
                    .build();

            return anthropicClient.messages().create(params).content().stream()
                    .flatMap(block -> block.text().stream())
                    .findFirst()
                    .map(textBlock -> textBlock.text())
                    .orElseThrow(() -> new TripickException(ErrorCode.AI_API_ERROR));
        } catch (TripickException e) {
            throw e;
        } catch (Exception e) {
            throw new TripickException(ErrorCode.AI_API_ERROR);
        }
    }

    private String buildPrompt(
            RecommendationRequest request,
            List<FestivalCandidate> festivalCandidates,
            List<TravelSpotCandidate> spotCandidates
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("당신은 여행 추천 전문가입니다. 아래 사용자 취향과 후보 목록을 보고 가장 어울리는 축제와 여행지를 골라주세요.\n\n");
        sb.append("[사용자 취향]\n");
        sb.append("- 지역: ").append(request.getRegion() == null ? "전국" : request.getRegion()).append("\n");
        sb.append("- 관심사: ").append(request.getInterests() == null ? "무관" : String.join(", ", request.getInterests())).append("\n");
        sb.append("- 이동수단: ").append(request.getTransportation() == null ? "무관" : request.getTransportation()).append("\n\n");

        sb.append("[축제 후보 목록]\n");
        for (FestivalCandidate festival : festivalCandidates) {
            sb.append("- festivalId=").append(festival.getFestivalId())
                    .append(", name=").append(festival.getName())
                    .append(", region=").append(festival.getRegion())
                    .append(", category=").append(festival.getCategory())
                    .append(", period=").append(festival.getStartDate()).append("~").append(festival.getEndDate())
                    .append("\n");
        }

        sb.append("\n[여행지 후보 목록]\n");
        for (TravelSpotCandidate spot : spotCandidates) {
            sb.append("- spotId=").append(spot.getSpotId())
                    .append(", name=").append(spot.getName())
                    .append(", region=").append(spot.getRegion())
                    .append(", category=").append(spot.getCategory())
                    .append("\n");
        }

        sb.append("\n위 후보 목록에 있는 festivalId/spotId만 사용해서 최대 5개의 축제와 최대 5개의 여행지를 추천해주세요.");
        return sb.toString();
    }
}
