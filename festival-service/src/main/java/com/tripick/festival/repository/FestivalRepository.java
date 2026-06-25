package com.tripick.festival.repository;

import com.tripick.festival.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    Optional<Festival> findByApiId(String apiId);

    @Query("""
        select f from Festival f
        where (:region is null or f.region like concat('%', :region, '%'))
        and (:category is null or f.category = :category)
        and (:monthStart is null or f.startDate <= :monthEnd)
        and (:monthStart is null or f.endDate >= :monthStart)
        """)
    Page<Festival> search(
            @Param("region") String region,
            @Param("category") String category,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd,
            Pageable pageable);
}
