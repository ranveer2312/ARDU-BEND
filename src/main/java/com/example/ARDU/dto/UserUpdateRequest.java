package com.example.ARDU.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    // Immutable fields not included: email, mobileNumber

    private String name;
    private String password;
    private String dlNumber;
    private String fatherName;
    private LocalDate dateOfBirth;
    private String badgeNumber;
    private String address;
    private String bloodGroup;
    private LocalDate dateOfJoiningOrRenewal; // admin may set this
    private String whatsappNumber;
    
    
}
