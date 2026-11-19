package com.semilleros.backend.controller;

import com.semilleros.backend.dto.CapacitacionRequestDTO;
import com.semilleros.backend.dto.CapacitacionResponseDTO;
import com.semilleros.backend.service.CapacitacionService;
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
@RequestMapping("/api/capacitaciones")
@RequiredArgsConstructor
@Tag(name = "Capacitaciones", description = "Operaciones CRUD para la gestión de capacitaciones realizadas por los semilleros")
public class CapacitacionController {

    private final CapacitacionService service;

    @GetMapping
    @Operation(summary = "Listar capacitaciones", description = "Retorna todas las capacitaciones realizadas por los semilleros")
    public ResponseEntity<List<CapacitacionResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/semillero/{semilleroId}")
    @Operation(summary = "Listar capacitaciones por semillero", description = "Retorna las capacitaciones correspondientes a un semillero específico")
    public ResponseEntity<List<CapacitacionResponseDTO>> getBySemilleroId(@PathVariable Long semilleroId) {
        return ResponseEntity.ok(service.findBySemilleroId(semilleroId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener capacitación por ID", description = "Retorna una capacitación según su identificador")
    public ResponseEntity<CapacitacionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear capacitación", description = "Registra una nueva capacitación realizada por un semillero")
    public ResponseEntity<CapacitacionResponseDTO> create(@Valid @RequestBody CapacitacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar capacitación", description = "Actualiza los datos de una capacitación existente")
    public ResponseEntity<CapacitacionResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CapacitacionRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar capacitación", description = "Elimina una capacitación por su identificador")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
