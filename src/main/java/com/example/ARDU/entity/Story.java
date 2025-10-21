package com.example.ARDU.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentUrl;
    private LocalDateTime createdAt;

    // Approval and expiration fields
    private LocalDateTime expiresAt;
    private boolean approved = false;

    // Fields to track the approver
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    private String approvedByUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // Original creator of the story
}