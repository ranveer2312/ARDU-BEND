package com.example.ARDU.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AdminUpdateRequest {
    // Fields that can be updated. Do NOT include mainAdmin here (only via transfer endpoint).
    private String name;
    private String mobileNumber;
    private String password; // optional: if provided, will be hashed and saved
    private String whatsappNumber;
    private String dlNumber;
    private String fatherName;
    private LocalDate dateOfBirth;
    private String badgeNumber;
    private String address;
    private String bloodGroup;
}
