package com.example.ARDU.repository;

import com.example.ARDU.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByApprovedTrueAndExpiresAtAfter(LocalDateTime now);

    // New method: Find posts whose expiration time has passed
    List<Post> findByExpiresAtBefore(LocalDateTime before);
}