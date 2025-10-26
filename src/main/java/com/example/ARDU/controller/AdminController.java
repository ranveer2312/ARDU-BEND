package com.example.ARDU.controller;

import com.example.ARDU.dto.AccountCreateRequest;
import com.example.ARDU.dto.ImageUploadResponse;
import com.example.ARDU.entity.Admin;
import com.example.ARDU.entity.User;
import com.example.ARDU.repository.AdminRepository;
import com.example.ARDU.repository.UserRepository;
import com.example.ARDU.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * AdminController
 * - createAdmin: only MAIN_ADMIN can call
 * - approveUser: ADMIN or MAIN_ADMIN
 * - rejectUser: ADMIN or MAIN_ADMIN
 */
@RestController
// 🛑 FIX 1 & 2: Change path to plural 'admins' and add CORS annotation
@RequestMapping("/api/admins")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🛑 FIX 3: ADD MISSING GET ENDPOINT
    /**
     * Get admin profile by ID. Crucial for the frontend ProfilePage.
     */
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        return adminRepository.findById(id)
                .map(a -> {
                    a.setPasswordHash(null); // Clear sensitive data before returning
                    return ResponseEntity.ok(a);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new admin. Only MAIN_ADMIN should be allowed to call this.
     */
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@RequestBody AccountCreateRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (adminRepository.existsByEmail(req.getEmail().toLowerCase())) {
            return ResponseEntity.badRequest().body("Admin email already exists");
        }

        Admin a = new Admin();
        a.setName(req.getName());
        a.setEmail(req.getEmail().toLowerCase());
        a.setMobileNumber(req.getMobileNumber());
        a.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        a.setMainAdmin(false);
        a.setEmailVerified(false);
        a.setMobileVerified(false);

        // Optional fields
        a.setDlNumber(req.getDlNumber());
        a.setFatherName(req.getFatherName());
        a.setDateOfBirth(req.getDateOfBirth());
        a.setBadgeNumber(req.getBadgeNumber());
        a.setAddress(req.getAddress());
        a.setBloodGroup(req.getBloodGroup());
        a.setWhatsappNumber(req.getWhatsappNumber());
        // Nominee info
        a.setNomineeName(req.getNomineeName());
        a.setNomineeRelationship(req.getNomineeRelationship());
        a.setNomineeContactNumber(req.getNomineeContactNumber());

        adminRepository.save(a);

        a.setPasswordHash(null);
        return ResponseEntity.ok(a);
    }

    /**
     * Get all pending users (ADMIN or MAIN_ADMIN).
     */
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    @GetMapping("/users/pending")
    public ResponseEntity<?> getPendingUsers() {
        var pendingUsers = userRepository.findByApprovalStatus("PENDING");
        // Remove password hashes for security
        pendingUsers.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(pendingUsers);
    }

    /**
     * Approve a user (any ADMIN or MAIN_ADMIN can call this).
     */
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable("id") Long id) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null) {
            return ResponseEntity.notFound().build();
        }

        LocalDate doj = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        u.setApprovalStatus("APPROVED");
        u.setDateOfJoiningOrRenewal(doj);
        u.setExpiryDate(doj.plusDays(364));
        u.setActive(true);
        u.setCreatedAt(Instant.now()); // approval timestamp
        u.setUpdatedAt(Instant.now());

        userRepository.save(u);

        u.setPasswordHash(null);
        return ResponseEntity.ok(u);
    }

    /**
     * Reject a user (ADMIN or MAIN_ADMIN).
     */
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    @PutMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable("id") Long id, @RequestBody(required = false) String reason) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null) {
            return ResponseEntity.notFound().build();
        }
        u.setApprovalStatus("REJECTED");
        u.setActive(false);
        u.setUpdatedAt(Instant.now());
        userRepository.save(u);
        return ResponseEntity.ok("User rejected");
    }

    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadAdminImage(@PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Admin a = adminRepository.findById(id).orElse(null);
        if (a == null)
            return ResponseEntity.notFound().build();

        // if caller is ADMIN (not MAIN_ADMIN), ensure they can only upload for
        // themselves
        String principalEmail = (authentication != null) ? authentication.getName() : null;
        boolean isAdminRoleOnly = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_ADMIN"));
        boolean isMainAdmin = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_MAIN_ADMIN"));

        if (isAdminRoleOnly && !isMainAdmin) {
            if (principalEmail == null || !principalEmail.equalsIgnoreCase(a.getEmail())) {
                return ResponseEntity.status(403).body("Admin can only upload their own image");
            }
        }

        try {
            String folder = "ardu_admins";
            var res = imageService.upload(file, folder);
            a.setImageUrl(res.getUrl());
            a.setImagePublicId(res.getPublicId());
            a.setUpdatedAt(Instant.now());
            adminRepository.save(a);
            return ResponseEntity.ok(new ImageUploadResponse(res.getUrl(), res.getPublicId(), "Uploaded"));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Upload failed: " + ex.getMessage());
        }
    }
}
