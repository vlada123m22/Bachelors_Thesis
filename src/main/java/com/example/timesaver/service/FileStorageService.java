package com.example.timesaver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.directory}")
    private String uploadDirectory;

    /**
     * Store file and return the file path
     */
    public String storeFile(MultipartFile file, Long projectId, Long applicantId, Integer questionNumber)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Validate file size (e.g., max 10MB)
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new IOException("File size exceeds maximum limit of 10MB");
        }

        // Create directory structure: uploadDirectory/projectId/applicantId/
        Path projectDir = Paths.get(uploadDirectory, String.valueOf(projectId));
        Path applicantDir = projectDir.resolve(String.valueOf(applicantId));

        // Create directories if they don't exist
        Files.createDirectories(applicantDir);

        // Generate unique filename to avoid collisions
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("q%d_%s_%s%s",
                questionNumber, timestamp, uniqueId, fileExtension);

        // Full file path
        Path filePath = applicantDir.resolve(filename);

        // Copy file to destination
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for storage in database
        return String.format("%d/%d/%s", projectId, applicantId, filename);
    }

    /**
     * Delete file from storage
     */
    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        Path fullPath = Paths.get(uploadDirectory, filePath);
        Files.deleteIfExists(fullPath);
    }

    /**
     * Get absolute path for a stored file
     */
    public Path getFilePath(String relativePath) {
        return Paths.get(uploadDirectory, relativePath);
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String relativePath) {
        Path fullPath = Paths.get(uploadDirectory, relativePath);
        return Files.exists(fullPath);
    }

    /**
     * Get file size
     */
    public long getFileSize(String relativePath) throws IOException {
        Path fullPath = Paths.get(uploadDirectory, relativePath);
        return Files.size(fullPath);
    }
}
