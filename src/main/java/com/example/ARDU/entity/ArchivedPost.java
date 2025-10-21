package com.example.ARDU.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Preserve original post data
    private Long originalPostId;
    private String contentUrl;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime archivedAt;

    @ManyToOne
    private User user;
}