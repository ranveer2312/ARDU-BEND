package com.example.ARDU.controller;

import com.example.ARDU.dto.PostRequest;
// ... (other imports)
import com.example.ARDU.entity.Comment;
import com.example.ARDU.entity.Post;
import com.example.ARDU.entity.Reaction;
import com.example.ARDU.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

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
        return ResponseEntity.ok(postService.getAllVisiblePosts());
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
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
}