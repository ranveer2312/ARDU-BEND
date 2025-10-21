package com.example.ARDU.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CloudinaryService {

    // Inject the configured Cloudinary object from CloudinaryConfig
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // Define the maximum duration for a story video: 1 minute (60 seconds)
    private static final int MAX_VIDEO_DURATION_SECONDS = 60;
    
    // Cloudinary folder path (Can be injected from properties if needed)
    private static final String FOLDER_PATH = "ardu_media";

    /**
     * Uploads the file, checks if it's a video, and if so, trims it to 1 minute (60s).
     *
     * @param file The file to upload.
     * @return The secure URL of the uploaded and processed content.
     */
    public String uploadAndProcessFile(MultipartFile file) {
        try {
            // 1. Prepare upload options
            Map<String, Object> options = new HashMap<>();
            options.put("resource_type", "auto"); // Detect image, video, raw, etc.
            options.put("folder", FOLDER_PATH);    // Organize uploads in a specific folder
            
            String mimeType = file.getContentType();

            // 2. Apply video trimming transformation if it's a video
            if (mimeType != null && mimeType.startsWith("video/")) {
                System.out.println("Processing and trimming video to max " + MAX_VIDEO_DURATION_SECONDS + " seconds: " + file.getOriginalFilename());

                // Create the transformation object to limit duration and auto-select codec
                Transformation transformation = new Transformation()
                    .videoCodec("auto")
                    .duration("lte:" + MAX_VIDEO_DURATION_SECONDS); 
                    
                options.put("transformation", transformation);
            } else {
                 System.out.println("Uploading image/other file: " + file.getOriginalFilename());
            }

            // 3. Perform the actual upload to Cloudinary
            Map uploadResult = cloudinary.uploader()
                .upload(file.getBytes(), options);

            // 4. Return the secure URL of the uploaded asset
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            // Handle I/O errors during file processing (e.g., file read error)
            System.err.println("Cloudinary upload failed due to I/O error: " + e.getMessage());
            throw new RuntimeException("File upload failed.", e);
        } catch (Exception e) {
            // Handle exceptions from the Cloudinary API
            System.err.println("Cloudinary upload failed: " + e.getMessage());
            throw new RuntimeException("Could not upload content to Cloudinary.", e);
        }
    }

    /**
     * Deletes the file from Cloudinary using its public ID.
     * @param contentUrl The secure URL of the content.
     */
    public void deleteFile(String contentUrl) {
        try {
            String publicId = extractPublicIdFromUrl(contentUrl);
            
            if (publicId != null) {
                // Determine resource type by checking if the ID contains the folder path
                String resourceType = publicId.contains("/video/") ? "video" : "image";
                
                // Perform the destruction (deletion)
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
                System.out.println("Cloudinary: Successfully deleted asset with Public ID: " + publicId);
            } else {
                 System.out.println("Cloudinary: Could not determine Public ID from URL: " + contentUrl);
            }
        } catch (Exception e) {
            System.err.println("Cloudinary deletion failed for URL: " + contentUrl + ". Error: " + e.getMessage());
        }
    }

    /**
     * Helper method to extract the public ID (including the folder path) from a Cloudinary URL.
     * This method is more robust than a simple split.
     */
    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // Regex to capture the path after the version number and before the extension
        // Example URL format: .../upload/v1600000000/ardu_media/my_video.mp4
        // The captured group is (ardu_media/my_video)
        Pattern pattern = Pattern.compile("/v\\d+/[a-zA-Z]+/(.+?)(?:\\.\\w+)?$");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            // The public ID is the full path including the folder and file name (without extension)
            String fullPath = matcher.group(1);
            int dotIndex = fullPath.lastIndexOf('.');
            return (dotIndex != -1) ? fullPath.substring(0, dotIndex) : fullPath;
        }
        return null;
    }
}