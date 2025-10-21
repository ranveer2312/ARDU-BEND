package com.example.ARDU.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {

    private final Cloudinary cloudinary;

    public ImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads the file to Cloudinary under specified folder and returns a small map with secure_url and public_id.
     */
    public UploadResult upload(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        Map<String, Object> params = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "auto",    // allow images and other resources
                "overwrite", true,
                "use_filename", false,
                "unique_filename", true
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        String secureUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        return new UploadResult(secureUrl, publicId);
    }

    public static class UploadResult {
        private final String url;
        private final String publicId;
        public UploadResult(String url, String publicId) {
            this.url = url;
            this.publicId = publicId;
        }
        public String getUrl() { return url; }
        public String getPublicId() { return publicId; }
    }
}
