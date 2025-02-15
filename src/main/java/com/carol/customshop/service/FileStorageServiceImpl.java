package com.carol.customshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements com.carol.customshop.service.FileStorageService {
    private final Path fileStorageLocation;
    private final String baseImagePath;

    public FileStorageServiceImpl(
            @Value("${product.images.upload-dir}") String uploadDir,
            @Value("${product.images.base-path}") String baseImagePath) {
        this.baseImagePath = baseImagePath;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(fileStorageLocation); // Ensure the directory exists
        } catch (IOException e) {
            throw new RuntimeException("Could not create the directory to store files at: " + uploadDir, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        // Generate a unique filename
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetLocation = fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return baseImagePath + fileName;
    }

    @Override
    public Path getFilePath(String fileName) {
        return fileStorageLocation.resolve(fileName);
    }

    @Override
    public void ensureUploadDirectoryExists() {
        try {
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create the directory to store files.", e);
        }
    }
}
