package com.semilleros.backend.controller;

import com.semilleros.backend.dto.CapacitacionRequestDTO;
import com.semilleros.backend.dto.CapacitacionResponseDTO;
import com.semilleros.backend.service.CapacitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/capacitaciones")
@RequiredArgsConstructor
@Tag(name = "Capacitaciones", description = "Operaciones CRUD para capacitaciones")
public class CapacitacionController {

    private final CapacitacionService service;

    @Operation(summary = "Crear una nueva capacitación")
    @PostMapping
    public ResponseEntity<CapacitacionResponseDTO> create(@Valid @RequestBody CapacitacionRequestDTO dto) {
        CapacitacionResponseDTO created = service.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Obtener todas las capacitaciones")
    @GetMapping
    public ResponseEntity<List<CapacitacionResponseDTO>> getAll() {
        List<CapacitacionResponseDTO> list = service.findAll();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtener una capacitación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CapacitacionResponseDTO> getById(@PathVariable Long id) {
        CapacitacionResponseDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Actualizar una capacitación existente")
    @PutMapping("/{id}")
    public ResponseEntity<CapacitacionResponseDTO> update(@PathVariable Long id,
                                                          @Valid @RequestBody CapacitacionRequestDTO dto) {
        CapacitacionResponseDTO updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar una capacitación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

