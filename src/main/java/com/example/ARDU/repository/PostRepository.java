package com.example.ARDU.repository;

import com.example.ARDU.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // ❌ Error-prone derived method: List<Post>
    // findByApprovedAndExpiresAtAfter(LocalDateTime now);

    // ✅ NEW: Use JPQL @Query for unambiguous query logic
    @Query("SELECT p FROM Post p WHERE p.approved = true AND p.expiresAt > :now")
    List<Post> findVisiblePosts(@Param("now") LocalDateTime now);

    // Method for pending posts (works fine as it only has one boolean condition)
    List<Post> findByApprovedFalse();

    // Existing method: Find posts whose expiration time has passed
    List<Post> findByExpiresAtBefore(LocalDateTime before);

    List<Post> findByUserIdAndApprovedOrderByCreatedAtDesc(Long userId, boolean approved);
    
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Post> findByUserOrderByCreatedAtDesc(@Param("userId") Long userId);
}