package com.example.ARDU.config;

import com.example.ARDU.entity.Admin;
import com.example.ARDU.repository.AdminRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@Configuration
public class MainAdminBootstrap {
  private final AdminRepository repo;
  private final PasswordEncoder encoder;

  public MainAdminBootstrap(AdminRepository repo, PasswordEncoder encoder){
    this.repo = repo; this.encoder = encoder;
  }

  @Bean
  ApplicationRunner initMainAdmin(Environment env){
    return args -> {
      if(repo.existsByMainAdminTrue()) return;

      String email = Optional.ofNullable(env.getProperty("bootstrap.main-admin.email")).orElse("mainadmin@example.com");
      String name = Optional.ofNullable(env.getProperty("bootstrap.main-admin.name")).orElse("Main Admin");
      String mobile = Optional.ofNullable(env.getProperty("bootstrap.main-admin.mobile")).orElse("9999999999");
      String rawPassword = Optional.ofNullable(env.getProperty("bootstrap.main-admin.password")).orElseGet(MainAdminBootstrap::generateRandomPwd);

      Admin a = new Admin();
      a.setName(name);
      a.setEmail(email.toLowerCase());
      a.setMobileNumber(mobile);
      a.setPasswordHash(encoder.encode(rawPassword));
      a.setMainAdmin(true);
      repo.save(a);

      // Log credentials (visible in console only on first run)
      System.out.println("=== MAIN ADMIN BOOTSTRAPPED ===");
      System.out.println("email: " + email);
      System.out.println("password: " + rawPassword);
      System.out.println("==============================");
    };
  }

  private static String generateRandomPwd(){
    var r = new java.security.SecureRandom();
    byte[] b = new byte[12];
    r.nextBytes(b);
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }
}
