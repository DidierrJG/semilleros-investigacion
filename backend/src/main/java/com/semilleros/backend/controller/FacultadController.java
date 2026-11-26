package com.semilleros.backend.controller;

import com.semilleros.backend.dto.FacultadRequestDTO;
import com.semilleros.backend.dto.FacultadResponseDTO;
import com.semilleros.backend.service.FacultadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facultades")
@RequiredArgsConstructor
@Tag(name = "Facultades", description = "Gestión de facultades institucionales y sus representantes")
public class FacultadController {

    private final FacultadService facultadService;

    @Operation(summary = "Listar todas las facultades")
    @GetMapping
    public ResponseEntity<List<FacultadResponseDTO>> findAll() {
        return ResponseEntity.ok(facultadService.findAll());
    }

    @Operation(summary = "Obtener facultad por ID")
    @GetMapping("/{id}")
    public ResponseEntity<FacultadResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(facultadService.findById(id));
    }

    @Operation(summary = "Crear nueva facultad")
    @PostMapping
    public ResponseEntity<FacultadResponseDTO> create(@Valid @RequestBody FacultadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultadService.create(request));
    }

    @Operation(summary = "Actualizar facultad existente")
    @PutMapping("/{id}")
    public ResponseEntity<FacultadResponseDTO> update(@PathVariable Long id, @Valid @RequestBody FacultadRequestDTO request) {
        return ResponseEntity.ok(facultadService.update(id, request));
    }

    @Operation(summary = "Eliminar facultad")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        facultadService.delete(id);
    }
}
