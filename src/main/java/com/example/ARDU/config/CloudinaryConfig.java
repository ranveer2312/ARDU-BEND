package com.example.ARDU.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    // 🚨 UPDATED to use keys matching your application.properties 🚨

    // The key in application.properties is 'cloudinary.cloud_name' (underscore)
    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    // The key in application.properties is 'cloudinary.api_key' (underscore)
    @Value("${cloudinary.api_key}")
    private String apiKey;

    // The key in application.properties is 'cloudinary.api_secret' (underscore)
    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Use ObjectUtils.asMap() to create the configuration map
        // The keys here must match what the Cloudinary SDK expects
        Map<String, Object> config = ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true // Recommended: Forces HTTPS URLs
        );
        return new Cloudinary(config);
    }
}