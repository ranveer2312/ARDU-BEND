package com.example.ARDU.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties({"posts", "passwordHash", "mobileNumber", "whatsappNumber", "emailVerified", "mobileVerified", "dlNumber", "fatherName", "dateOfBirth", "badgeNumber", "address", "bloodGroup", "nomineeName", "nomineeRelationship", "nomineeContactNumber", "role", "approvalStatus", "dateOfJoiningOrRenewal", "expiryDate", "active", "createdAt", "updatedAt"})
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({"user", "comments", "reactions"})
    private Post post;
}
