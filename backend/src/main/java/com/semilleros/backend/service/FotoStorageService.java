package com.semilleros.backend.service;

import com.semilleros.backend.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FotoStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    @Value("${app.upload.dir:uploads/semilleros}")
    private String uploadDir;

    public String storeSemilleroFoto(Long semilleroId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo de imagen no puede estar vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("El archivo de imagen no puede superar los 5 MB");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("Formato de imagen no permitido. Solo se aceptan: JPG, PNG, WEBP, GIF");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException("El archivo proporcionado no es una imagen válida");
        }

        try {
            Path targetDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetDirectory);

            String filename = "semillero_" + semilleroId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
            Path destinationPath = targetDirectory.resolve(filename).normalize();

            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/semilleros/" + filename;
        } catch (IOException e) {
            throw new BusinessException("Error al almacenar la imagen en el dispositivo: " + e.getMessage());
        }
    }

    public void deleteFoto(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.isBlank() || !fotoUrl.startsWith("/uploads/semilleros/")) {
            return;
        }

        try {
            String filename = Paths.get(fotoUrl).getFileName().toString();
            Path targetFile = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename).normalize();
            Files.deleteIfExists(targetFile);
        } catch (Exception ignored) {
            // Silencioso al limpiar archivos anteriores
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
