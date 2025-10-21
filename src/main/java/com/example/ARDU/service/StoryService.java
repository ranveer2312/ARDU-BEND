package com.example.ARDU.service;

import com.example.ARDU.entity.Story;
import com.example.ARDU.entity.User;
import com.example.ARDU.repository.StoryRepository;
import com.example.ARDU.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public StoryService(StoryRepository storyRepository,
            UserRepository userRepository,
            CloudinaryService cloudinaryService) {
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // 1. Create a new story (UPDATED: Handles file upload and trimming)
    @Transactional
    public Story createStory(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Story file cannot be empty.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Upload file and apply 1-minute trim in CloudinaryService
        // The service layer delegates the complex media task
        String contentUrl = cloudinaryService.uploadAndProcessFile(file);

        Story story = new Story();
        story.setUser(user);
        story.setContentUrl(contentUrl);
        story.setCreatedAt(LocalDateTime.now());
        // 2. Set expiration to 24 hours from creation
        story.setExpiresAt(LocalDateTime.now().plusHours(24));

        // 3. LOGIC: Admin/MainAdmin stories are auto-approved.
        String userRole = user.getRole();
        if ("ADMIN".equals(userRole) || "MAIN_ADMIN".equals(userRole)) {
            story.setApproved(true);
            story.setApprovedByUsername(user.getUsername());
        } else {
            story.setApproved(false);
        }

        return storyRepository.save(story);
    }

    // 2. Approve/Reject a story and record the approver
    @Transactional
    public Story approveStory(Long storyId, boolean approved, String adminUsername) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Approving Admin not found"));

        if (story.isApproved() != approved) {
            story.setApproved(approved);

            if (approved) {
                story.setApprovedBy(admin);
                story.setApprovedByUsername(admin.getUsername());
            } else {
                story.setApprovedBy(null);
                story.setApprovedByUsername(null);
            }
            return storyRepository.save(story);
        }
        return story;
    }

    // 3. Get publicly visible stories (approved AND not expired)
    public List<Story> getVisibleStories() {
        return storyRepository.findByApprovedTrueAndExpiresAtAfter(LocalDateTime.now());
    }

    // 4. Delete a story (Removes from DB and Cloud)
    @Transactional
    public void deleteStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        // Delete from cloud storage first
        cloudinaryService.deleteFile(story.getContentUrl());

        storyRepository.delete(story);
    }

    // 5. NEW METHOD: Get all expired stories for Admin archive view
    public List<Story> getExpiredStories() {
        return storyRepository.findByExpiresAtBefore(LocalDateTime.now());
    }

    // 6. NEW METHOD: Delete all expired stories (Cleanup/Archiving action)
    @Transactional
    public int deleteExpiredStories() {
        List<Story> expiredStories = storyRepository.findByExpiresAtBefore(LocalDateTime.now());
        int deleteCount = expiredStories.size();

        for (Story story : expiredStories) {
            // Delete content from Cloudinary
            cloudinaryService.deleteFile(story.getContentUrl());
            // Delete entity from database
            storyRepository.delete(story);
        }
        return deleteCount;
    }

    // NOTE: For automatic cleanup, you should add a @Scheduled method here or in a
    // separate ArchivingService.
}