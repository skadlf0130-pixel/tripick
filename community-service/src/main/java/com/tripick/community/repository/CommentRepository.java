package com.tripick.community.repository;

import com.tripick.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdAndIsDeletedFalse(Long postId, Pageable pageable);

    Optional<Comment> findByIdAndPostId(Long id, Long postId);
}
