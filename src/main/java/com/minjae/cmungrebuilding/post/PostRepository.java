package com.minjae.cmungrebuilding.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Long, Post> {
    List<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
