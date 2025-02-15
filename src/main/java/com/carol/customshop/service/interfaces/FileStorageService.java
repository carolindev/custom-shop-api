package com.carol.customshop.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    String storeFile(MultipartFile file) throws IOException;
    Path getFilePath(String fileName);
    void ensureUploadDirectoryExists();
}