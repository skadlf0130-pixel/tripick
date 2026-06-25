package com.tripick.festival.repository;

import com.tripick.festival.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndFestivalId(Long userId, Long festivalId);

    Optional<Bookmark> findByUserIdAndFestivalId(Long userId, Long festivalId);

    Page<Bookmark> findByUserId(Long userId, Pageable pageable);
}
