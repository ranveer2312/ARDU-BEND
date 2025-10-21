package com.example.ARDU.controller;

import com.example.ARDU.dto.StoryRequest;
import com.example.ARDU.entity.Story;
import com.example.ARDU.service.StoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    // Helper method to get the authenticated user's username
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // 1. Create a new story (UPDATED: Accepts MultipartFile in the request body)
    @PostMapping("/create")
    // FIX: Added 'ROLE_' prefix to all roles
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_MAIN_ADMIN')")
    public ResponseEntity<Story> createStory(@ModelAttribute StoryRequest request) {
        Story story = storyService.createStory(request.getUserId(), request.getFile());
        return ResponseEntity.ok(story);
    }

    // 2. Approve/Reject a story
    @PutMapping("/{storyId}/approve")
    // FIX: Added 'ROLE_' prefix to all roles
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MAIN_ADMIN')")
    public ResponseEntity<Story> approveStory(@PathVariable Long storyId, @RequestParam boolean approved) {

        String adminUsername = getCurrentUsername();

        Story story = storyService.approveStory(storyId, approved, adminUsername);
        return ResponseEntity.ok(story);
    }

    // 3. Get all active stories (Users only see approved and unexpired)
    @GetMapping
    // FIX: Added 'ROLE_' prefix to all roles
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_MAIN_ADMIN')")
    public ResponseEntity<List<Story>> getAllStories() {
        List<Story> stories = storyService.getVisibleStories();
        return ResponseEntity.ok(stories);
    }

    // 4. Delete a story by ID (For Admin deletion)
    @DeleteMapping("/{storyId}")
    // FIX: Added 'ROLE_' prefix to all roles
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MAIN_ADMIN')")
    public ResponseEntity<Void> deleteStory(@PathVariable Long storyId) {
        storyService.deleteStory(storyId);
        return ResponseEntity.noContent().build();
    }

    // 5. NEW ENDPOINT: Get all expired stories (Archive View for Admin)
    @GetMapping("/archive/expired")
    // FIX: Added 'ROLE_' prefix to all roles
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MAIN_ADMIN')")
    public ResponseEntity<List<Story>> getExpiredStories() {
        List<Story> expired = storyService.getExpiredStories();
        return ResponseEntity.ok(expired);
    }

    // 6. NEW ENDPOINT: Admin can manually trigger deletion of all expired posts
    // (Cleanup)
    @DeleteMapping("/archive/cleanup")
    // FIX: Added 'ROLE_' prefix to all roles
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MAIN_ADMIN')")
    public ResponseEntity<String> cleanupExpiredStories() {
        int count = storyService.deleteExpiredStories();
        return ResponseEntity.ok("Deleted " + count + " expired stories from the database and cloud storage.");
    }
}