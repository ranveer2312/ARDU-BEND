package com.example.ARDU.controller;

import com.example.ARDU.dto.PostRequest;
import com.example.ARDU.entity.Comment;
import com.example.ARDU.entity.Post;
import com.example.ARDU.entity.Reaction;
import com.example.ARDU.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // =================================================================
    // NEW ENDPOINT FOR ADMIN PENDING POSTS
    // =================================================================

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    public ResponseEntity<List<Post>> getPendingPosts() {
        List<Post> posts = postService.getPendingPosts();
        return ResponseEntity.ok(posts != null ? posts : List.of());
    }

    @GetMapping("/user/{userId}/pending")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    public ResponseEntity<List<Post>> getUserPendingPosts(@PathVariable Long userId) {
        List<Post> posts = postService.getUserPendingPosts(userId);
        return ResponseEntity.ok(posts != null ? posts : List.of());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    public ResponseEntity<List<Post>> getUserPosts(@PathVariable Long userId) {
        List<Post> posts = postService.getUserPosts(userId);
        return ResponseEntity.ok(posts != null ? posts : List.of());
    }

    // =================================================================
    // EXISTING ENDPOINTS
    // =================================================================

    // @ModelAttribute is required to correctly bind the MultipartFile from the
    // request

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    public ResponseEntity<Post> createPost(@ModelAttribute PostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @PutMapping("/{postId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    public ResponseEntity<Post> approvePost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.approvePost(postId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllVisiblePosts();
        return ResponseEntity.ok(posts != null ? posts : List.of());
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    public ResponseEntity<Void> deleteUserPost(@PathVariable Long postId) {
        postService.deleteUserPost(postId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    public ResponseEntity<Post> updatePost(@PathVariable Long postId, @RequestBody PostRequest request) {
        Post updatedPost = postService.updatePost(postId, request);
        return ResponseEntity.ok(updatedPost);
    }

    @PostMapping("/{postId}/reaction")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    public ResponseEntity<String> addReaction(
            @PathVariable Long postId,
            @RequestParam String username,
            @RequestParam String reactionType) {
        postService.addReaction(postId, username, reactionType);
        return ResponseEntity.ok("Reaction saved permanently!");
    }

    @PostMapping("/{postId}/comment")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")

    public ResponseEntity<String> addComment(
            @PathVariable Long postId,
            @RequestParam String username,
            @RequestParam String text) {
        postService.addComment(postId, username, text);
        return ResponseEntity.ok("Comment saved permanently!");
    }

    @GetMapping("/{postId}/comments")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")

    public ResponseEntity<Page<Comment>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getComments(postId, page, size));
    }

    @GetMapping("/{postId}/reactions")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    public ResponseEntity<Page<Reaction>> getReactions(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getReactions(postId, page, size));
    }

    @GetMapping("/{postId}/reactions/summary")
    public ResponseEntity<Map<String, Object>> getReactionSummary(@PathVariable Long postId) {
        try {
            String currentUsername = null;
            try {
                currentUsername = org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication().getName();
            } catch (Exception e) {
                System.out.println("No authentication context available");
            }
            System.out.println("Getting reaction summary for post: " + postId + ", user: " + currentUsername);
            return ResponseEntity.ok(postService.getReactionSummary(postId, currentUsername));
        } catch (Exception e) {
            System.err.println("Error getting reaction summary: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}