package com.example.ARDU.controller;

import com.example.ARDU.dto.AdminResponse;
import com.example.ARDU.dto.AdminUpdateRequest;
import com.example.ARDU.entity.Admin;
import com.example.ARDU.repository.AdminRepository;
import com.example.ARDU.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * MAIN_ADMIN -> Transfer main admin to another admin (by id).
     */
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    @PutMapping("/main-admin/{id}")
    public ResponseEntity<?> makeMainAdmin(@PathVariable Long id) {
        Admin updated = adminService.transferMainAdmin(id);
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * MAIN_ADMIN -> Delete a non-main admin.
     */
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok().build();
    }

    /**
     * List all admins. Allowed for ADMIN and MAIN_ADMIN.
     */
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdminResponse>> listAdmins() {
        List<AdminResponse> list = adminRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Edit admin profile.
     * - MAIN_ADMIN can edit any admin.
     * - ADMIN can edit only their own profile.
     */
    @PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> editAdmin(@PathVariable Long id,
                                       @RequestBody AdminUpdateRequest req,
                                       Authentication authentication) {

        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }

        String principalEmail = (authentication != null) ? authentication.getName() : null;

        boolean callerIsMainAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_MAIN_ADMIN"));

        boolean callerIsAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        // If caller is ADMIN (but not MAIN_ADMIN), restrict to editing own profile
        if (callerIsAdmin && !callerIsMainAdmin) {
            if (principalEmail == null || !principalEmail.equalsIgnoreCase(admin.getEmail())) {
                return ResponseEntity.status(403).body("Admins can only edit their own profile");
            }
        }

        // apply updates (only non-null fields)
        if (req.getName() != null) admin.setName(req.getName());
        if (req.getMobileNumber() != null) admin.setMobileNumber(req.getMobileNumber());
        if (req.getWhatsappNumber() != null) admin.setWhatsappNumber(req.getWhatsappNumber());
        if (req.getDlNumber() != null) admin.setDlNumber(req.getDlNumber());
        if (req.getFatherName() != null) admin.setFatherName(req.getFatherName());
        if (req.getDateOfBirth() != null) admin.setDateOfBirth(req.getDateOfBirth());
        if (req.getBadgeNumber() != null) admin.setBadgeNumber(req.getBadgeNumber());
        if (req.getAddress() != null) admin.setAddress(req.getAddress());
        if (req.getBloodGroup() != null) admin.setBloodGroup(req.getBloodGroup());

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            admin.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        admin.setUpdatedAt(java.time.Instant.now());
        adminRepository.save(admin);

        return ResponseEntity.ok(toResponse(admin));
    }

    private AdminResponse toResponse(Admin a) {
        return new AdminResponse(
                a.getId(),
                a.getName(),
                a.getEmail(),
                a.getMobileNumber(),
                a.getMainAdmin(),
                a.getImageUrl()
        );
    }

   
// DELETE an admin account (ADMIN can delete non-Main Admin, MAIN_ADMIN can delete any non-Main Admin)
// DELETE an admin account
@PreAuthorize("hasAnyRole('ADMIN','MAIN_ADMIN')")
@DeleteMapping("/delete/{id}")
public ResponseEntity<?> deleteAdminAccount(@PathVariable Long id, Authentication authentication) {
    Admin adminToDelete = adminRepository.findById(id).orElse(null);
    if (adminToDelete == null) return ResponseEntity.notFound().build();

    boolean isMainAdminToDelete = Boolean.TRUE.equals(adminToDelete.getMainAdmin());
    String callerEmail = authentication.getName();
    boolean callerIsMainAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_MAIN_ADMIN"));
    boolean callerIsAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_ADMIN"));

    // Main Admin can delete anyone
    if (callerIsMainAdmin) {
        adminRepository.delete(adminToDelete);
        return ResponseEntity.ok("Admin account deleted successfully by Main Admin");
    }

    // Normal admin can delete themselves or other non-main admins
    if (callerIsAdmin && !callerIsMainAdmin) {
        if (isMainAdminToDelete) {
            return ResponseEntity.status(403).body("Cannot delete Main Admin account");
        }

        if (!callerEmail.equalsIgnoreCase(adminToDelete.getEmail()) && !callerIsMainAdmin) {
            return ResponseEntity.status(403).body("Admins can only delete themselves or other non-Main Admins");
        }

        adminRepository.delete(adminToDelete);
        return ResponseEntity.ok("Admin account deleted successfully");
    }

    return ResponseEntity.status(403).body("Unauthorized to delete this admin");
}



}
