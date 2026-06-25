package com.tripick.recommendation.repository;

import com.tripick.recommendation.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    Optional<Recommendation> findFirstByConditionKeyAndExpiresAtAfterOrderByIdDesc(String conditionKey, LocalDateTime now);
}
