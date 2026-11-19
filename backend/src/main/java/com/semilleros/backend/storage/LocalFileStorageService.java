package com.semilleros.backend.storage;

import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml"
    );

    public LocalFileStorageService(@Value("${app.storage.base-path:/home/semilleros}") String basePath) {
        Path resolved;
        try {
            resolved = Paths.get(basePath).toAbsolutePath().normalize();
            Files.createDirectories(resolved);
        } catch (Exception e) {
            String fallback = System.getProperty("user.home") + "/semilleros";
            log.warn("No se pudo usar basePath '{}'. Usando ruta alternativa de desarrollo: {}", basePath, fallback);
            resolved = Paths.get(fallback).toAbsolutePath().normalize();
            try {
                Files.createDirectories(resolved);
            } catch (IOException ex) {
                throw new IllegalStateException("No se pudo crear el directorio de almacenamiento: " + resolved, ex);
            }
        }
        this.rootLocation = resolved;
        log.info("Almacenamiento de archivos inicializado en: {}", this.rootLocation);
    }

    @PostConstruct
    public void init() {
        for (FileCategory cat : FileCategory.values()) {
            try {
                Path dir = rootLocation.resolve(cat.getSubDirectory());
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
            } catch (IOException e) {
                log.error("No se pudo inicializar subdirectorio para categoría {}: {}", cat.name(), e.getMessage());
            }
        }
    }

    @Override
    public String storeFile(FileCategory category, MultipartFile file, String filenamePrefix) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo proporcionado está vacío o no es válido");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Formato de imagen no permitido. Formatos aceptados: JPG, PNG, WEBP, GIF, SVG");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "archivo");
        String extension = "";
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx >= 0) {
            extension = originalFilename.substring(dotIdx).toLowerCase();
        } else {
            extension = contentType.equalsIgnoreCase("image/png") ? ".png" : ".jpg";
        }

        String prefix = (filenamePrefix != null && !filenamePrefix.isBlank()) ? filenamePrefix.trim() : "file";
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String targetFilename = prefix + "-" + uniqueSuffix + extension;

        try {
            Path categoryDir = rootLocation.resolve(category.getSubDirectory()).normalize().toAbsolutePath();
            if (!Files.exists(categoryDir)) {
                Files.createDirectories(categoryDir);
            }

            Path targetLocation = categoryDir.resolve(targetFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo guardado en: {} (categoría: {})", targetLocation, category.name());
            return targetFilename;
        } catch (IOException ex) {
            throw new BusinessException("Error al almacenar el archivo: " + ex.getMessage());
        }
    }

    @Override
    public Resource loadFileAsResource(FileCategory category, String filename) {
        try {
            Path categoryDir = rootLocation.resolve(category.getSubDirectory()).normalize().toAbsolutePath();
            Path filePath = categoryDir.resolve(filename).normalize();

            if (!filePath.startsWith(categoryDir)) {
                throw new BusinessException("Ruta de archivo no permitida (violación de seguridad)");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("No se encontró el archivo solicitado: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Error al resolver URL del archivo: " + filename);
        }
    }

    @Override
    public String determineContentType(String filename, Resource resource) {
        try {
            String probed = Files.probeContentType(resource.getFile().toPath());
            if (probed != null) return probed;
        } catch (Exception ignored) {
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    @Override
    public void deleteFile(FileCategory category, String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path categoryDir = rootLocation.resolve(category.getSubDirectory()).normalize().toAbsolutePath();
            Path filePath = categoryDir.resolve(filename).normalize();
            if (filePath.startsWith(categoryDir)) {
                Files.deleteIfExists(filePath);
                log.info("Archivo eliminado: {}", filePath);
            }
        } catch (Exception e) {
            log.warn("No se pudo eliminar el archivo {}: {}", filename, e.getMessage());
        }
    }
}
