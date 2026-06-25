package com.tripick.festival.repository;

import com.tripick.festival.entity.TravelSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TravelSpotRepository extends JpaRepository<TravelSpot, Long> {

    @Query("""
        select s from TravelSpot s
        where (:region is null or s.region like concat('%', :region, '%'))
        and (:category is null or s.category = :category)
        """)
    Page<TravelSpot> search(
            @Param("region") String region,
            @Param("category") String category,
            Pageable pageable);
}
