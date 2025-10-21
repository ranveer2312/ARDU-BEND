package com.example.ARDU.service;

import com.example.ARDU.dto.PostRequest;
import com.example.ARDU.entity.*;
import com.example.ARDU.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository; // Assuming this exists
    private final ReactionRepository reactionRepository; // Assuming this exists
    private final CommentRepository commentRepository; // Assuming this exists
    private final CloudinaryService cloudinaryService;

    public PostService(PostRepository postRepository,
            UserRepository userRepository,
            ReactionRepository reactionRepository,
            CommentRepository commentRepository,
            CloudinaryService cloudinaryService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Post createPost(PostRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new RuntimeException("Media file is required for a post.");
        }

        // 1. Process file (upload, validate, trim video)
        String contentUrl = cloudinaryService.uploadAndProcessFile(request.getFile());

        Post post = new Post();
        post.setUser(user);
        post.setContentUrl(contentUrl); // Set the processed URL
        post.setCreatedAt(LocalDateTime.now());

        if (request.isStory()) {
            post.setExpiresAt(LocalDateTime.now().plusHours(24));
            post.setType("STORY");
            post.setApproved(true);
        } else {
            // Regular POST expires in 7 days (for archiving logic)
            post.setExpiresAt(LocalDateTime.now().plusDays(7));
            post.setType("POST");

            if (user.getRole().equalsIgnoreCase("ADMIN") ||
                    user.getRole().equalsIgnoreCase("SUPER_ADMIN")) {
                post.setApproved(true);
            } else {
                post.setApproved(false);
            }
        }

        return postRepository.save(post);
    }

    public Post approvePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setApproved(true);
        return postRepository.save(post);
    }

    public List<Post> getAllVisiblePosts() {
        return postRepository.findByApprovedTrueAndExpiresAtAfter(LocalDateTime.now());
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Delete the file from external storage before deleting the record
        cloudinaryService.deleteFile(post.getContentUrl());
        postRepository.delete(post);
    }

    public void addReaction(Long postId, String username, String type) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reaction reaction = new Reaction();
        reaction.setPost(post);
        reaction.setUser(user);
        reaction.setType(type);
        reaction.setCreatedAt(LocalDateTime.now());

        reactionRepository.save(reaction);
    }

    public void addComment(Long postId, String username, String text) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    // Paginated comments
    public Page<Comment> getComments(Long postId, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentRepository.findByPost(post, PageRequest.of(page, size));
    }

    // Paginated reactions
    public Page<Reaction> getReactions(Long postId, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return reactionRepository.findByPost(post, PageRequest.of(page, size));
    }
}