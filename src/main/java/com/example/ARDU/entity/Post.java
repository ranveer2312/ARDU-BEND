package com.example.ARDU.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentUrl;
    private String caption;
    private String type; // POST / STORY
    private boolean approved;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // Used for archiving logic

    @ManyToOne
    @JsonIgnoreProperties({"posts", "passwordHash", "mobileNumber", "whatsappNumber", "emailVerified", "mobileVerified"})
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("post")
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("post")
    private List<Reaction> reactions;
}