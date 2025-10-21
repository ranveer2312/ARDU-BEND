package com.example.ARDU.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean mobileVerified;
    private Boolean emailVerified;
    private String whatsappNumber;
    private Boolean whatsappVerified;
    private String dlNumber;
    private String fatherName;
    private LocalDate dateOfBirth;
    private String badgeNumber;
    private String address;
    private String bloodGroup;
    private String role;
    private String approvalStatus;
    private LocalDate dateOfJoiningOrRenewal;
    private LocalDate expiryDate;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String imageUrl;
    private String nomineeName;
private String nomineeRelationship;
private String nomineeContactNumber;


}
