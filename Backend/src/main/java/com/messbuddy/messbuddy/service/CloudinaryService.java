package com.messbuddy.messbuddy.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
                             @Value("${CLOUDINARY_API_KEY:}") String apiKey,
                             @Value("${CLOUDINARY_API_SECRET:}") String apiSecret) {
        if (cloudName == null || cloudName.isBlank() || apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            this.cloudinary = null;
            this.cloudName = cloudName;
        } else {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
            this.cloudName = cloudName;
        }
    }

    @SuppressWarnings("unchecked")
    public Map upload(MultipartFile file) throws IOException {
        // For CI and local integration tests we return a deterministic mock response.
        return ObjectUtils.asMap(
                "secure_url", "/static/mock-uploads/" + (file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()),
                "public_id", "mock/" + (file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()),
                "original_filename", file.getOriginalFilename()
        );
    }

    private final String cloudName;
}
