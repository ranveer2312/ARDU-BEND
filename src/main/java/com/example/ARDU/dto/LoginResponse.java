package com.example.ARDU.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
    private Boolean mainAdmin;     // useful if caller is admin
    private JwtResponse jwt;
}
