package com.example.ARDU.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class PostRequest {
    private Long userId;
    private MultipartFile file;
    private String caption;
    private boolean story;
}
