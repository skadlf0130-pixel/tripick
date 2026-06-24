package com.tripick.community.repository;

import com.tripick.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByIsDeletedFalse(Pageable pageable);

    Optional<Post> findByIdAndIsDeletedFalse(Long id);

    @Query("select p from Post p left join fetch p.media where p.id = :id and p.isDeleted = false")
    Optional<Post> findDetailByIdAndIsDeletedFalse(@Param("id") Long id);
}
