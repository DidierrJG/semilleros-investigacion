package com.semilleros.backend.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(FileCategory category, MultipartFile file, String filenamePrefix);
    Resource loadFileAsResource(FileCategory category, String filename);
    String determineContentType(String filename, Resource resource);
    void deleteFile(FileCategory category, String filename);
}
