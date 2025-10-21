package com.example.ARDU.repository;

import com.example.ARDU.entity.ArchivedPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivedPostRepository extends JpaRepository<ArchivedPost, Long> {
}