package com.example.ARDU.controller;

import com.example.ARDU.dto.JwtResponse;
import com.example.ARDU.dto.LoginRequest;
import com.example.ARDU.dto.LoginResponse;
import com.example.ARDU.entity.Admin;
import com.example.ARDU.entity.User;
import com.example.ARDU.repository.AdminRepository;
import com.example.ARDU.repository.UserRepository;
import com.example.ARDU.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // --- User login ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().toLowerCase());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        User user = userOpt.get();

        if (!"APPROVED".equalsIgnoreCase(user.getApprovalStatus())
                || user.getActive() == null || !user.getActive()) {
            return ResponseEntity.status(403).body("User not approved or inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        Long expires = null;
        try {
            expires = jwtUtil.getExpirationSeconds();
        } catch (Exception ignored) {
        }

        JwtResponse jwtResp = new JwtResponse(token, "Bearer", expires);

        LoginResponse resp = new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(), // use username here
                user.getRole(),
                false,
                jwtResp);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // true in production HTTPS
                .path("/")
                .maxAge(expires != null ? expires : 86400)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(resp);
    }

    // --- Admin login ---
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(loginRequest.getEmail().toLowerCase());
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        Admin admin = adminOpt.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String role = Boolean.TRUE.equals(admin.getMainAdmin()) ? "MAIN_ADMIN" : "ADMIN";

        String token = jwtUtil.generateToken(admin.getEmail(), role);
        Long expires = null;
        try {
            expires = jwtUtil.getExpirationSeconds();
        } catch (Exception ignored) {
        }

        JwtResponse jwtResp = new JwtResponse(token, "Bearer", expires);

        LoginResponse resp = new LoginResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getName(),
                role,
                Boolean.TRUE.equals(admin.getMainAdmin()),
                jwtResp);

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // true for production
                .path("/")
                .maxAge(expires != null ? expires : 86400)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(resp);
    }
}
