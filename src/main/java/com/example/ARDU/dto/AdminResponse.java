package com.example.ARDU.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminResponse {
    private Long id;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean mainAdmin;
    private String imageUrl;
}
