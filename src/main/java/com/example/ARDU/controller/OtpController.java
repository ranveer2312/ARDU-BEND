package com.example.ARDU.controller;

import com.example.ARDU.entity.User;
import com.example.ARDU.repository.UserRepository;
import com.example.ARDU.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;
    private final UserRepository userRepository;

    public OtpController(OtpService otpService, UserRepository userRepository) {
        this.otpService = otpService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send/{channel}")
    public ResponseEntity<String> sendOtp(
            @PathVariable String channel,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String whatsapp) {

        Optional<User> userOptional = switch (channel.toLowerCase()) {
            case "email" -> email != null
                    ? userRepository.findByEmail(email)
                    : Optional.empty();
            case "sms" -> mobile != null
                    ? userRepository.findByMobileNumber(mobile)
                    : Optional.empty();
            case "whatsapp" -> whatsapp != null
                    ? userRepository.findByWhatsappNumber(whatsapp)
                    : Optional.empty();
            default -> Optional.empty();
        };

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found for channel: " + channel);
        }

        otpService.sendOtp(userOptional.get(), channel);
        return ResponseEntity.ok("OTP sent via " + channel);
    }

    @PostMapping("/verify/{channel}")
    public ResponseEntity<String> verifyOtp(
            @PathVariable String channel,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String whatsapp,
            @RequestParam String otp) {

        Optional<User> userOptional = switch (channel.toLowerCase()) {
            case "email" -> email != null
                    ? userRepository.findByEmail(email)
                    : Optional.empty();
            case "sms" -> mobile != null
                    ? userRepository.findByMobileNumber(mobile)
                    : Optional.empty();
            case "whatsapp" -> whatsapp != null
                    ? userRepository.findByWhatsappNumber(whatsapp)
                    : Optional.empty();
            default -> Optional.empty();
        };

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found for channel: " + channel);
        }

        boolean isVerified = otpService.verifyOtp(userOptional.get(), otp);

        if (isVerified) {
            User user = userOptional.get();
            switch (channel.toLowerCase()) {
                case "email" -> user.setEmailVerified(true);
                case "sms" -> user.setMobileVerified(true);
                case "whatsapp" -> user.setWhatsappVerified(true);
            }
            userRepository.save(user);

            return ResponseEntity.ok("OTP verified successfully via " + channel + "!");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }
    }
}
