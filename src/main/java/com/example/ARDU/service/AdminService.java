package com.example.ARDU.service;

import com.example.ARDU.entity.Admin;
import com.example.ARDU.repository.AdminRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    @Transactional
    public Admin transferMainAdmin(Long targetAdminId) {
        Admin target = adminRepository.findById(targetAdminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + targetAdminId));

        // If already main, nothing to do
        if (Boolean.TRUE.equals(target.getMainAdmin())) {
            return target;
        }

        // Demote current main if exists
        adminRepository.findByMainAdminTrue().ifPresent(curr -> {
            curr.setMainAdmin(false);
            adminRepository.save(curr);
        });

        // Promote new main
        target.setMainAdmin(true);
        return adminRepository.save(target);
    }

    @Transactional
    public void deleteAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminId));

        if (Boolean.TRUE.equals(admin.getMainAdmin())) {
            throw new IllegalStateException("Cannot delete MAIN_ADMIN. Transfer main admin first.");
        }

        adminRepository.delete(admin);
    }
}
