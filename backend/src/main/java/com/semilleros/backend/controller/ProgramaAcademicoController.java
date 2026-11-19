package com.semilleros.backend.controller;

import com.semilleros.backend.dto.ProgramaAcademicoRequestDTO;
import com.semilleros.backend.dto.ProgramaAcademicoResponseDTO;
import com.semilleros.backend.service.ProgramaAcademicoService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/programas")
@RequiredArgsConstructor
@Tag(name = "Programas Académicos", description = "Operaciones CRUD para la gestión de programas académicos")
public class ProgramaAcademicoController {

    private final ProgramaAcademicoService programaService;

    @GetMapping
    @Operation(summary = "Listar programas", description = "Retorna todos los programas académicos registrados")
    public ResponseEntity<List<ProgramaAcademicoResponseDTO>> findAll() {
        return ResponseEntity.ok(programaService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener programa por ID", description = "Retorna un programa académico según su identificador")
    public ResponseEntity<ProgramaAcademicoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(programaService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear programa", description = "Registra un nuevo programa académico")
    public ResponseEntity<ProgramaAcademicoResponseDTO> create(@Valid @RequestBody ProgramaAcademicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programaService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar programa", description = "Actualiza el nombre de un programa académico existente")
    public ResponseEntity<ProgramaAcademicoResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProgramaAcademicoRequestDTO request) {
        return ResponseEntity.ok(programaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar programa", description = "Elimina un programa académico si no tiene integrantes asociados")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        programaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
