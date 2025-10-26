package com.example.ARDU.repository;

import com.example.ARDU.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByWhatsappNumber(String whatsappNumber);

    List<User> findAllByActiveTrueAndExpiryDateBefore(LocalDate date);

    Optional<User> findByEmail(String email);

    Optional<User> findByMobileNumber(String mobileNumber);

    Optional<User> findByWhatsappNumber(String whatsappNumber);

    // ADD THIS:
    Optional<User> findByUsername(String username);
    
    List<User> findByApprovalStatus(String approvalStatus);
}
