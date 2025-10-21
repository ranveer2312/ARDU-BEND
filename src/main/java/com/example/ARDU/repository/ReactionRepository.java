package com.example.ARDU.repository;

import com.example.ARDU.entity.Reaction;
import com.example.ARDU.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Page<Reaction> findByPost(Post post, Pageable pageable);
}
