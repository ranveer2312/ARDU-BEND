package com.example.ARDU.repository;

import com.example.ARDU.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);   // 👈 add this
    boolean existsByMainAdminTrue();       // to ensure only one MainAdmin
    Optional<Admin> findByMainAdminTrue();

}
