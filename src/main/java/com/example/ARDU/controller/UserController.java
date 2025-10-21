package com.example.ARDU.controller;

import com.example.ARDU.dto.*;
import com.example.ARDU.entity.User;
import com.example.ARDU.repository.UserRepository;
import com.example.ARDU.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    // --- Register ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AccountCreateRequest req) {

        // Validate required fields
        if (req.getName() == null || req.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Name is required");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (req.getMobileNumber() == null || req.getMobileNumber().isBlank()) {
            return ResponseEntity.badRequest().body("Mobile number is required");
        }

        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        if (userRepository.existsByMobileNumber(req.getMobileNumber())) {
            return ResponseEntity.badRequest().body("Mobile number already exists");
        }

        LocalDate doj = req.getDateOfJoiningOrRenewal() != null
                ? req.getDateOfJoiningOrRenewal()
                : LocalDate.now(ZoneId.of("Asia/Kolkata"));

        User u = new User();
        // Corrected line to set both name and username fields
        u.setUsername(req.getName());
        u.setName(req.getName()); // <-- Corrected line to fix the error
        u.setEmail(req.getEmail().toLowerCase());
        u.setMobileNumber(req.getMobileNumber());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setDlNumber(req.getDlNumber());
        u.setFatherName(req.getFatherName());
        u.setDateOfBirth(req.getDateOfBirth());
        u.setBadgeNumber(req.getBadgeNumber());
        u.setAddress(req.getAddress());
        u.setBloodGroup(req.getBloodGroup());
        u.setWhatsappNumber(req.getWhatsappNumber());

        u.setNomineeName(req.getNomineeName());
        u.setNomineeRelationship(req.getNomineeRelationship());
        u.setNomineeContactNumber(req.getNomineeContactNumber());

        u.setRole("USER");
        u.setApprovalStatus("PENDING");
        u.setDateOfJoiningOrRenewal(doj);
        u.setExpiryDate(doj.plusDays(364));
        u.setActive(false);

        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());

        userRepository.save(u);

        u.setPasswordHash(null); // hide password
        return ResponseEntity.ok(toResponse(u));
    }

    // --- Upload user image ---
    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadUserImage(@PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null)
            return ResponseEntity.notFound().build();

        String principalEmail = (authentication != null) ? authentication.getName() : null;
        boolean isUserRole = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
        if (isUserRole && !u.getEmail().equalsIgnoreCase(principalEmail)) {
            return ResponseEntity.status(403).body("Users can only upload their own image");
        }

        try {
            String folder = System.getProperty("cloudinary.folder.users") != null
                    ? System.getProperty("cloudinary.folder.users")
                    : "ardu_users";

            var res = imageService.upload(file, folder);
            u.setImageUrl(res.getUrl());
            u.setImagePublicId(res.getPublicId());
            u.setUpdatedAt(Instant.now());
            userRepository.save(u);

            ImageUploadResponse resp = new ImageUploadResponse(res.getUrl(), res.getPublicId(), "Uploaded");
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Upload failed: " + ex.getMessage());
        }
    }

    // --- Get user by ID ---
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id, Authentication authentication) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null)
            return ResponseEntity.notFound().build();

        String principalEmail = authentication != null ? authentication.getName() : null;

        boolean callerIsMainAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_MAIN_ADMIN"));
        boolean callerIsAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"));
        boolean callerIsUser = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_USER"));

        if (callerIsUser && !callerIsAdmin && !callerIsMainAdmin) {
            if (principalEmail == null || !principalEmail.equalsIgnoreCase(u.getEmail())) {
                return ResponseEntity.status(403).body("Users can only view their own profile");
            }
        }

        return ResponseEntity.ok(toResponse(u));
    }

    // --- Update user ---
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> editUser(@PathVariable Long id,
            @RequestBody UserUpdateRequest req,
            Authentication authentication) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null)
            return ResponseEntity.notFound().build();

        String principalEmail = authentication != null ? authentication.getName() : null;
        boolean callerIsMainAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_MAIN_ADMIN"));
        boolean callerIsAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"));
        boolean callerIsUser = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_USER"));

        if (callerIsUser && !callerIsAdmin && !callerIsMainAdmin) {
            if (principalEmail == null || !principalEmail.equalsIgnoreCase(user.getEmail())) {
                return ResponseEntity.status(403).body("Users can only edit their own profile");
            }
        }

        if (req.getName() != null) {
            user.setUsername(req.getName());
            user.setName(req.getName());
        }
        if (req.getDlNumber() != null)
            user.setDlNumber(req.getDlNumber());
        if (req.getFatherName() != null)
            user.setFatherName(req.getFatherName());
        if (req.getDateOfBirth() != null)
            user.setDateOfBirth(req.getDateOfBirth());
        if (req.getBadgeNumber() != null)
            user.setBadgeNumber(req.getBadgeNumber());
        if (req.getAddress() != null)
            user.setAddress(req.getAddress());
        if (req.getBloodGroup() != null)
            user.setBloodGroup(req.getBloodGroup());
        if (req.getWhatsappNumber() != null)
            user.setWhatsappNumber(req.getWhatsappNumber());

        if ((callerIsAdmin || callerIsMainAdmin) && req.getDateOfJoiningOrRenewal() != null) {
            user.setDateOfJoiningOrRenewal(req.getDateOfJoiningOrRenewal());
            user.setExpiryDate(req.getDateOfJoiningOrRenewal().plusDays(364));
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return ResponseEntity.ok(toResponse(user));
    }

    // --- List all users ---
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    @GetMapping
    public ResponseEntity<?> listUsers() {
        List<UserResponse> list = userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // --- Delete user ---
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAIN_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null)
            return ResponseEntity.notFound().build();

        String principalEmail = authentication != null ? authentication.getName() : null;
        boolean callerIsMainAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_MAIN_ADMIN"));
        boolean callerIsAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
        boolean callerIsUser = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_USER"));

        if (callerIsUser && !callerIsAdmin && !callerIsMainAdmin) {
            if (principalEmail == null || !principalEmail.equalsIgnoreCase(user.getEmail())) {
                return ResponseEntity.status(403).body("Users can only delete their own account");
            }
        }

        userRepository.delete(user);
        return ResponseEntity.ok("User account deleted successfully");
    }

    // --- Helper mapping ---
    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getMobileNumber(),
                u.getMobileVerified(),
                u.getEmailVerified(),
                u.getWhatsappNumber(),
                u.getWhatsappVerified(),
                u.getDlNumber(),
                u.getFatherName(),
                u.getDateOfBirth(),
                u.getBadgeNumber(),
                u.getAddress(),
                u.getBloodGroup(),
                u.getRole(),
                u.getApprovalStatus(),
                u.getDateOfJoiningOrRenewal(),
                u.getExpiryDate(),
                u.getActive(),
                u.getCreatedAt(),
                u.getUpdatedAt(),
                u.getImageUrl(),
                u.getNomineeName(),
                u.getNomineeRelationship(),
                u.getNomineeContactNumber());
    }
}