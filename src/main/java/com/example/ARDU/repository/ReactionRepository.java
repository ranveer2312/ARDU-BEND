package com.example.ARDU.repository;

import com.example.ARDU.entity.Reaction;
import com.example.ARDU.entity.Post;
import com.example.ARDU.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Page<Reaction> findByPost(Post post, Pageable pageable);
    
    @Modifying
    @Transactional
    void deleteByPostAndUser(Post post, User user);
}
