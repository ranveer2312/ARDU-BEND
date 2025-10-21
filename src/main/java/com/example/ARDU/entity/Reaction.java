package com.example.ARDU.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // LIKE / DISLIKE
    private LocalDateTime createdAt;

    @ManyToOne
    private User user; // changed from AppUser to User

    @ManyToOne
    private Post post;
}
