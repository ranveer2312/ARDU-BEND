package com.example.ARDU.service;

import com.example.ARDU.dto.PostRequest;
import com.example.ARDU.entity.*;
import com.example.ARDU.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    // ... (Dependencies and constructor remain the same)

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final AdminRepository adminRepository;

    public PostService(PostRepository postRepository,
            UserRepository userRepository,
            ReactionRepository reactionRepository,
            CommentRepository commentRepository,
            CloudinaryService cloudinaryService,
            AdminRepository adminRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.cloudinaryService = cloudinaryService;
        this.adminRepository = adminRepository;
    }

    // =================================================================
    // NEW METHOD FOR ADMIN REVIEW
    // =================================================================

    /**
     * Retrieves all posts that are currently pending admin approval (approved =
     * false).
     * * @return List of pending Post entities.
     */
    public List<Post> getPendingPosts() {
        return postRepository.findByApprovedFalse();
    }

    /**
     * Retrieves all posts for a specific user that are currently pending admin
     * approval (approved =
     * false), ordered by creation date (newest first).
     * 
     * @param userId The ID of the user whose pending posts are to be retrieved.
     * @return List of pending Post entities for the specified user.
     */
    public List<Post> getUserPendingPosts(Long userId) {
        return postRepository.findByUserIdAndApprovedOrderByCreatedAtDesc(userId, false);
    }

    // =================================================================
    // EXISTING METHODS
    // =================================================================

    public Post createPost(PostRequest request) {
        // ... (implementation remains the same)
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
        post.setCaption(request.getCaption());
        post.setCreatedAt(LocalDateTime.now());

        if (request.isStory()) {
            post.setExpiresAt(LocalDateTime.now().plusHours(24));
            post.setType("STORY");
            post.setApproved(true); // Stories are automatically approved
        } else {
            // Regular POST expires in 7 days (for archiving logic)
            post.setExpiresAt(LocalDateTime.now().plusDays(7));
            post.setType("POST");

            // Debug logging
            System.out.println("User role: '" + user.getRole() + "'");
            System.out.println("User ID: " + user.getId());
            System.out.println("User email: " + user.getEmail());

            // Check if user is admin by role or by checking admin table
            boolean isAdmin = false;
            if (user.getRole() != null &&
                    (user.getRole().equalsIgnoreCase("ADMIN") ||
                            user.getRole().equalsIgnoreCase("MAIN_ADMIN"))) {
                isAdmin = true;
                System.out.println("Admin detected by role");
            } else {
                // Check if user exists in admin table
                var adminOpt = adminRepository.findByEmail(user.getEmail());
                if (adminOpt.isPresent()) {
                    isAdmin = true;
                    System.out.println("Admin detected by admin table lookup");
                }
            }

            if (isAdmin) {
                System.out.println("Admin detected - auto-approving post");
                post.setApproved(true); // Admins bypass approval
            } else {
                System.out.println("Regular user - post requires approval");
                post.setApproved(false); // Regular users require approval
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

    public Post rejectPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setApproved(false);
        // You might want to add additional rejection logic here
        return postRepository.save(post);
    }

    /**
     * Retrieves all visible posts (approved and not expired) from the database.
     * * @return A list of visible posts.
     */
    public List<Post> getAllVisiblePosts() {
        // Calling the method using the @Query annotation
        return postRepository.findVisiblePosts(LocalDateTime.now());
    }

    public Map<String, Object> getReactionSummary(Long postId, String currentUsername) {
        System.out.println("=== GET REACTION SUMMARY DEBUG ===");
        System.out.println("PostId: " + postId + ", Username: " + currentUsername);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        System.out.println("Post found: " + post.getId());

        // Get all reactions for this post
        List<Reaction> reactions = reactionRepository.findByPost(post, PageRequest.of(0, 1000)).getContent();
        System.out.println("Found " + reactions.size() + " reactions");

        // Count reactions by type
        Map<String, Long> reactionCounts = reactions.stream()
                .collect(Collectors.groupingBy(Reaction::getType, Collectors.counting()));

        // Find current user's reaction
        String userReaction = null;
        if (currentUsername != null) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .or(() -> userRepository.findByEmail(currentUsername))
                    .orElse(null);

            if (currentUser != null) {
                System.out.println("Current user found: " + currentUser.getId());
                userReaction = reactions.stream()
                        .filter(r -> r.getUser().getId().equals(currentUser.getId()))
                        .map(Reaction::getType)
                        .findFirst()
                        .orElse(null);
                System.out.println("User reaction: " + userReaction);
            } catch (Exception e) {
                System.out.println("Current user not found for username: " + currentUsername);
            }
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("counts", reactionCounts);
        summary.put("userReaction", userReaction);
        summary.put("total", reactions.size());
        System.out.println("Returning summary: " + summary);
        return summary;
    }

    public void deletePost(Long postId) {
        // ... (implementation remains the same)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        cloudinaryService.deleteFile(post.getContentUrl());
        postRepository.delete(post);
    }

    // ... (addReaction, addComment, getComments, getReactions remain the same)
    public void addReaction(Long postId, String username, String type) {
        System.out.println("=== ADD REACTION DEBUG ===");
        System.out.println("PostId: " + postId + ", Username: " + username + ", Type: " + type);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        System.out.println("Post found: " + post.getId());

        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + user.getId() + ", Role: " + user.getRole());

        // Remove any existing reaction from this user on this post
        try {
            reactionRepository.deleteByPostAndUser(post, user);
            System.out.println("Previous reactions deleted");
        } catch (Exception e) {
            System.out.println("Error deleting previous reactions: " + e.getMessage());
        }

        // Add new reaction
        Reaction reaction = new Reaction();
        reaction.setPost(post);
        reaction.setUser(user);
        reaction.setType(type);
        reaction.setCreatedAt(LocalDateTime.now());

        reactionRepository.save(reaction);
        System.out.println("Reaction saved successfully");
    }

    public void addComment(Long postId, String username, String text) {
        System.out.println("=== ADD COMMENT DEBUG ===");
        System.out.println("PostId: " + postId + ", Username: " + username + ", Text: " + text);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        System.out.println("Post found: " + post.getId());

        User user = findUserForReactionOrComment(username);
        System.out.println("User found: " + user.getId() + ", Role: " + user.getRole());

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
        System.out.println("Comment saved successfully");
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

    public List<Post> getUserPosts(Long userId) {
        return postRepository.findByUserOrderByCreatedAtDesc(userId);
    }

    public void deleteUserPost(Long postId) {
        System.out.println("=== DELETE USER POST DEBUG ===");
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Get current user from security context
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        System.out.println("Current username from security context: " + currentUsername);

        // Try to find user by username first, then by email
        User currentUser = userRepository.findByUsername(currentUsername)
                .or(() -> userRepository.findByEmail(currentUsername))
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        System.out.println("Current user ID: " + currentUser.getId() + ", Role: " + currentUser.getRole());
        System.out.println("Post owner ID: " + post.getUser().getId());

        // Check if user owns the post or is admin
        if (!post.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equalsIgnoreCase("ADMIN") &&
                !currentUser.getRole().equalsIgnoreCase("MAIN_ADMIN")) {
            System.out.println("Access denied: User doesn't own the post");
            throw new RuntimeException("You can only delete your own posts");
        }

        System.out.println("Access granted: Deleting post");
        cloudinaryService.deleteFile(post.getContentUrl());
        postRepository.delete(post);
    }

    public Post updatePost(Long postId, PostRequest request) {
        System.out.println("=== UPDATE POST DEBUG ===");
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Get current user from security context
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        System.out.println("Current username from security context: " + currentUsername);

        // Try to find user by username first, then by email
        User currentUser = userRepository.findByUsername(currentUsername)
                .or(() -> userRepository.findByEmail(currentUsername))
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        System.out.println("Current user ID: " + currentUser.getId() + ", Role: " + currentUser.getRole());
        System.out.println("Post owner ID: " + post.getUser().getId());

        // Check if user owns the post or is admin
        if (!post.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equalsIgnoreCase("ADMIN") &&
                !currentUser.getRole().equalsIgnoreCase("MAIN_ADMIN")) {
            System.out.println("Access denied: User doesn't own the post");
            throw new RuntimeException("You can only edit your own posts");
        }

        System.out.println("Access granted: Updating post with caption: " + request.getCaption());
        // Update caption if provided
        if (request.getCaption() != null) {
            post.setCaption(request.getCaption());
        }

        return postRepository.save(post);
    }

    public List<Reaction> getReactionDetails(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return reactionRepository.findByPost(post, PageRequest.of(0, 100)).getContent();
    }

    private User findUserForReactionOrComment(String username) {
        // First try to find user by username or email
        Optional<User> userOpt = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username));

        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        // If not found, check if it's an admin and create/find corresponding user
        Optional<Admin> adminOpt = adminRepository.findByEmail(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            // Check if user already exists for this admin
            Optional<User> existingUser = userRepository.findByEmail(admin.getEmail());
            if (existingUser.isPresent()) {
                return existingUser.get();
            }

            // Create a user entry for the admin
            User adminUser = new User();
            adminUser.setName(admin.getName());
            adminUser.setUsername(admin.getName());
            adminUser.setEmail(admin.getEmail());
            adminUser.setMobileNumber(admin.getMobileNumber());
            adminUser.setPasswordHash(admin.getPasswordHash());
            adminUser.setRole(admin.getRole());
            adminUser.setApprovalStatus("APPROVED");
            adminUser.setActive(true);
            adminUser.setImageUrl(admin.getImageUrl());
            adminUser.setImagePublicId(admin.getImagePublicId());
            adminUser.setCreatedAt(admin.getCreatedAt());
            adminUser.setUpdatedAt(admin.getUpdatedAt());

            return userRepository.save(adminUser);
        }

        throw new RuntimeException("User not found: " + username);
    }
}