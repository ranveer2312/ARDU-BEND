package com.example.ARDU.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile; // <-- New Import

@Data
public class StoryRequest {
    private Long userId;
    // Changed from String contentUrl to accept the file directly
    private MultipartFile file; // <-- Changed type
}