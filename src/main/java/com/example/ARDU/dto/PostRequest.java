package com.example.ARDU.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class PostRequest {
    private Long userId;
    private MultipartFile file;
    private boolean story;
}
