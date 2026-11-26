package com.semilleros.backend.controller;

import com.semilleros.backend.dto.SemilleroPublicoResponseDTO;
import com.semilleros.backend.service.SemilleroPublicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/publico/semilleros")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Semilleros Públicos", description = "Endpoints abiertos de consulta pública de semilleros de investigación. No requieren autenticación.")
public class SemilleroPublicoController {

    private final SemilleroPublicoService semilleroPublicoService;

    @GetMapping
    @Operation(
        summary = "Listar todos los semilleros (público)",
        description = "Retorna la lista completa de semilleros con información pública: nombre, sigla, director, grupo, líneas e integrantes. No expone datos sensibles."
    )
    public ResponseEntity<List<SemilleroPublicoResponseDTO>> findAll() {
        return ResponseEntity.ok(semilleroPublicoService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener semillero por ID (público)",
        description = "Retorna la información pública completa de un semillero: identidad, integrantes, productos y capacitaciones."
    )
    public ResponseEntity<SemilleroPublicoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(semilleroPublicoService.findById(id));
    }
}
