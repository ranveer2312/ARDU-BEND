package com.example.ARDU.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic info
    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String passwordHash;

    @Column(nullable = false, unique = true)
    @JsonIgnore
    private String mobileNumber;

    @JsonIgnore
    private Boolean mobileVerified = false;
    @JsonIgnore
    private Boolean emailVerified = false;

    @JsonIgnore
    private String whatsappNumber;
    @JsonIgnore
    private Boolean whatsappVerified = false;

    private String role; // USER, ADMIN, SUPER_ADMIN

    private String approvalStatus; // PENDING, APPROVED, REJECTED

    // Personal details
    private String fatherName;
    private LocalDate dateOfBirth;
    private String dlNumber;
    private String badgeNumber;
    private String address;
    private String bloodGroup;

    // Nominee details
    private String nomineeName;
    private String nomineeRelationship;
    private String nomineeContactNumber;

    // Account & timestamps
    private Boolean active = false; // start inactive until approved
    private Instant createdAt;
    private Instant updatedAt;
    private LocalDate dateOfJoiningOrRenewal;
    private LocalDate expiryDate;

    // Media
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "image_public_id", length = 500)
    private String imagePublicId;

    // OTP
    @JsonIgnore
    private String otpCode;
    @JsonIgnore
    private Instant otpExpiry;
}