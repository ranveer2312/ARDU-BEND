package com.example.ARDU.service;

import com.example.ARDU.entity.ArchivedPost;
import com.example.ARDU.entity.Post;
import com.example.ARDU.repository.ArchivedPostRepository;
import com.example.ARDU.repository.PostRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArchivingService {

    private final PostRepository postRepository;
    private final ArchivedPostRepository archivedPostRepository;

    public ArchivingService(PostRepository postRepository, ArchivedPostRepository archivedPostRepository) {
        this.postRepository = postRepository;
        this.archivedPostRepository = archivedPostRepository;
    }

    /**
     * Scheduled task to move expired posts (older than 7 days) to the archive
     * table.
     * Runs daily at a fixed time (e.g., 2:00 AM).
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void archiveExpiredPosts() {
        // Find posts where the expiration time has already passed
        List<Post> postsToArchive = postRepository.findByExpiresAtBefore(LocalDateTime.now());

        if (!postsToArchive.isEmpty()) {
            postsToArchive.forEach(post -> {
                // 1. Create ArchivedPost from the old Post
                ArchivedPost archivedPost = new ArchivedPost(
                        null, // new ID
                        post.getId(), // original ID
                        post.getContentUrl(),
                        post.getType(),
                        post.getCreatedAt(),
                        LocalDateTime.now(), // set archive time
                        post.getUser());
                archivedPostRepository.save(archivedPost);

                // 2. Delete from the active Post table
                // NOTE: CascadeType.ALL on comments/reactions ensures they are deleted too
                postRepository.delete(post);
            });
            System.out.println("Archived " + postsToArchive.size() + " expired posts.");
        }
    }
}