package com.example.ARDU.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {
    private String name;
    private String email;
    private String mobileNumber;
    private String password;
    private String dlNumber;
    private String fatherName;
    private LocalDate dateOfBirth;
    private String badgeNumber;
    private String address;
    private String bloodGroup;
    private String whatsappNumber;
    private LocalDate dateOfJoiningOrRenewal;
    private String nomineeName;
private String nomineeRelationship;
private String nomineeContactNumber;
}
