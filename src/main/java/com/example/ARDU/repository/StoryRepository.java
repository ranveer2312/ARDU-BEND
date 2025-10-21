package com.example.ARDU.repository;

import com.example.ARDU.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {

    // Finds stories that are approved AND have not yet expired (for public view)
    List<Story> findByApprovedTrueAndExpiresAtAfter(LocalDateTime now);

    // List all expired stories (for Admin Archive/Cleanup)
    List<Story> findByExpiresAtBefore(LocalDateTime now);

    // This is redundant but kept from your original code.
    // List<Story> findByExpiresAtAfter(LocalDateTime now);
}