package com.semilleros.backend.controller;

import com.semilleros.backend.storage.FileCategory;
import com.semilleros.backend.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Tag(name = "Gestión de Archivos", description = "Servicio unificado de almacenamiento y consulta de imágenes, logos y evidencias")
public class ArchivoController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{categoria}/{filename:.+}")
    @Operation(summary = "Obtener archivo", description = "Descarga o visualiza una imagen o archivo por categoría (logos, integrantes, evidencias)")
    public ResponseEntity<Resource> getFile(
            @PathVariable String categoria,
            @PathVariable String filename) {
        FileCategory cat = FileCategory.fromString(categoria);
        Resource resource = fileStorageService.loadFileAsResource(cat, filename);
        String contentType = fileStorageService.determineContentType(filename, resource);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(resource);
    }

    @PostMapping(value = "/{categoria}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir archivo general", description = "Permite cargar imágenes de integrantes, evidencias u otros documentos")
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable String categoria,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prefix", required = false, defaultValue = "item") String prefix) {
        FileCategory cat = FileCategory.fromString(categoria);
        String filename = fileStorageService.storeFile(cat, file, prefix);
        String url = "/api/archivos/" + cat.getSubDirectory() + "/" + filename;

        return ResponseEntity.ok(Map.of(
                "filename", filename,
                "categoria", cat.getSubDirectory(),
                "url", url
        ));
    }
}
