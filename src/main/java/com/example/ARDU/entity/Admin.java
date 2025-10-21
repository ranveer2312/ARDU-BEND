package com.example.ARDU.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Info
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String mobileNumber;

    private Boolean mobileVerified = false;
    private Boolean emailVerified = false;
    private String whatsappNumber;
    private Boolean whatsappVerified = false;

    // Extra Info (optional for admins)
    private String dlNumber;
    private String fatherName;
    private LocalDate dateOfBirth;
    private String badgeNumber;
    private String address;
    private String bloodGroup;

    // Role flags
    @Column(nullable = false)
    private String role = "ADMIN";  // "ADMIN" or "MAIN_ADMIN"

    private Boolean mainAdmin = false;  // Only one can be true in DB

    // Lifecycle
    private Boolean active = true; // Admins are active by default

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

        @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "image_public_id", length = 500)
    private String imagePublicId;

    // inside Admin.java entity
private String nomineeName;
private String nomineeRelationship;
private String nomineeContactNumber;


}
