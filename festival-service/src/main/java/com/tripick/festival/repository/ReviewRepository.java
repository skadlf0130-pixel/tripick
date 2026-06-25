package com.tripick.festival.repository;

import com.tripick.festival.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByFestivalIdAndUserIdAndIsDeletedFalse(Long festivalId, Long userId);

    Optional<Review> findByIdAndFestivalId(Long id, Long festivalId);

    Page<Review> findByFestivalIdAndIsDeletedFalse(Long festivalId, Pageable pageable);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.festival.id = :festivalId and r.isDeleted = false")
    double getAverageRating(@Param("festivalId") Long festivalId);
}
