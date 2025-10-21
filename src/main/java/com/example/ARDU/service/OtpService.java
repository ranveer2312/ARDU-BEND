package com.example.ARDU.service;

import com.example.ARDU.entity.User;
import com.example.ARDU.repository.UserRepository;
import com.vonage.client.VonageClient;
import com.vonage.client.messages.MessageResponse;
import com.vonage.client.messages.MessagesClient;
import com.vonage.client.messages.MessageResponseException;
import com.vonage.client.messages.whatsapp.WhatsappTextRequest;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * OtpService - sends OTP via Email, SMS, and WhatsApp.
 * Uses Vonage SDK for SMS and optionally for production WhatsApp.
 * Uses VonageSandboxSender HTTP client when sandboxEnabled=true.
 */
@Service
public class OtpService {

    @Value("${vonage.api.key}")
    private String vonageApiKey;

    @Value("${vonage.api.secret}")
    private String vonageApiSecret;

    // production/sms sender in E.164 (e.g. +1415...)
    @Value("${vonage.from-number:}")
    private String vonageFromNumber;

    // sandbox toggle and sandbox 'from' (no plus) — used when vonage.sandbox.enabled=true
    @Value("${vonage.sandbox.enabled:false}")
    private boolean sandboxEnabled;

    // sandbox sender (no +, matches curl/dashboard examples)
    @Value("${vonage.sandbox.from-number:}")
    private String vonageSandboxFrom;

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final VonageSandboxSender sandboxSender;
    private VonageClient vonageClient;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public OtpService(UserRepository userRepository,
                      JavaMailSender mailSender,
                      VonageSandboxSender sandboxSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.sandboxSender = sandboxSender;
    }

    @PostConstruct
    public void initVonageClient() {
        try {
            this.vonageClient = VonageClient.builder()
                    .apiKey(vonageApiKey)
                    .apiSecret(vonageApiSecret)
                    .build();
        } catch (Exception e) {
            System.err.println("Failed to init VonageClient: " + e.getMessage());
            e.printStackTrace();
        }

        if (vonageFromNumber != null) {
            vonageFromNumber = vonageFromNumber.trim().replaceAll("\\s+", "");
        }
        if (vonageSandboxFrom != null) {
            vonageSandboxFrom = vonageSandboxFrom.trim().replaceAll("\\s+", "");
        }

        System.out.println("OtpService initialized: sandboxEnabled=" + sandboxEnabled +
                " sandboxFrom=" + vonageSandboxFrom + " fromNumber=" + vonageFromNumber);
    }

    // OTP Generator
    private String generateOtp() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

    // Send OTP via SMS using Vonage SDK
    public void sendOtpSms(String toPhone, String otp) {
        if (toPhone == null || toPhone.isBlank()) {
            System.err.println("sendOtpSms: missing toPhone");
            return;
        }
        if (vonageFromNumber == null || vonageFromNumber.isBlank()) {
            System.err.println("sendOtpSms: missing configured vonage.from-number");
            return;
        }

        String to = toPhone.replaceAll("\\s+", "");
        TextMessage message = new TextMessage(
                vonageFromNumber,
                to,
                "Your OTP is: " + otp + " (valid for 5 minutes)."
        );

        try {
            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);

            if (response.getMessages() == null || response.getMessages().isEmpty()) {
                System.err.println("SMS response missing messages array or it's empty. Full response: " + response);
                return;
            }

            var first = response.getMessages().get(0);
            MessageStatus status = first.getStatus();
            String errorText = first.getErrorText();

            if (status == MessageStatus.OK) {
                System.out.println("SMS sent OK. status=" + status);
            } else {
                System.err.println("Failed to send SMS. status=" + status + " error=" + errorText);
            }
        } catch (Exception ex) {
            System.err.println("Exception while sending SMS: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Send OTP via WhatsApp — uses sandbox HTTP endpoint when sandboxEnabled=true,
    // otherwise uses Vonage Java SDK (production).
    public void sendOtpWhatsapp(String toPhone, String otp) {
        if (toPhone == null || toPhone.isBlank()) {
            System.err.println("sendOtpWhatsapp: missing toPhone");
            return;
        }

        String text = "Your WhatsApp OTP is: " + otp + " (valid for 5 minutes).";

        // Sandbox path
        if (sandboxEnabled) {
            // sandbox expects 'to' without leading '+'
            String toNoPlus = toPhone.replaceAll("^\\+", "").replaceAll("\\s+", "");
            try {
                String uuid = sandboxSender.sendWhatsAppSandbox(toNoPlus, text);
                System.out.println("WhatsApp sandbox message UUID: " + uuid);
            } catch (Exception e) {
                System.err.println("Error sending whatsapp sandbox message: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        // Production path — use SDK
        if (vonageFromNumber == null || vonageFromNumber.isBlank()) {
            System.err.println("sendOtpWhatsapp: missing configured vonage.from-number");
            return;
        }

        try {
            MessagesClient messagesClient = vonageClient.getMessagesClient();

            String fromE164 = vonageFromNumber.trim().replaceAll("\\s+", "");
            String toE164 = toPhone.trim().replaceAll("\\s+", "");

            if (!fromE164.matches("^\\+\\d{6,15}$") || !toE164.matches("^\\+\\d{6,15}$")) {
                System.err.println("sendOtpWhatsapp: numbers not in E.164 format. from=" + fromE164 + " to=" + toE164);
                return;
            }

            WhatsappTextRequest request = WhatsappTextRequest.builder()
                    .from(fromE164)
                    .to(toE164)
                    .text(text)
                    .build();

            MessageResponse response = messagesClient.sendMessage(request);
            System.out.println("WhatsApp message UUID: " + response.getMessageUuid());
        } catch (MessageResponseException mre) {
            System.err.println("Vonage MessageResponseException: status=" + mre.getStatusCode() + " msg=" + mre.getMessage());
            mre.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Unexpected exception sending WhatsApp OTP: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Send OTP via Email using JavaMail
    public void sendOtpEmail(String toEmail, String otp) {
        if (toEmail == null || toEmail.isBlank()) {
            System.err.println("sendOtpEmail: missing toEmail");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp + " (valid for 5 minutes).");
            mailSender.send(message);
            System.out.println("OTP email sent to " + toEmail);
        } catch (Exception ex) {
            System.err.println("Failed to send OTP email: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // High-level send: stores OTP and dispatches via requested channel
    public void sendOtp(User user, String channel) {
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(Instant.now().plusSeconds(300));
        userRepository.save(user);

        switch (channel.toLowerCase()) {
            case "email":
                sendOtpEmail(user.getEmail(), otp);
                break;
            case "sms":
                sendOtpSms(user.getMobileNumber(), otp);
                break;
            case "whatsapp":
                sendOtpWhatsapp(user.getWhatsappNumber(), otp);
                break;
            default:
                System.err.println("Unknown OTP channel: " + channel);
        }
    }

    // Verify OTP stored on user
    public boolean verifyOtp(User user, String otp) {
        if (user.getOtpCode() != null && user.getOtpCode().equals(otp) &&
            user.getOtpExpiry() != null && user.getOtpExpiry().isAfter(Instant.now())) {

            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
