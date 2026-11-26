package com.semilleros.backend.controller;

import com.semilleros.backend.dto.DepartamentoRequestDTO;
import com.semilleros.backend.dto.DepartamentoResponseDTO;
import com.semilleros.backend.service.DepartamentoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
@Tag(name = "Departamentos", description = "Gestión de departamentos académicos y directores de departamento")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @Operation(summary = "Listar departamentos (opcionalmente filtrados por facultad)")
    @GetMapping
    public ResponseEntity<List<DepartamentoResponseDTO>> findAll(@RequestParam(required = false) Long facultadId) {
        if (facultadId != null) {
            return ResponseEntity.ok(departamentoService.findByFacultadId(facultadId));
        }
        return ResponseEntity.ok(departamentoService.findAll());
    }

    @Operation(summary = "Obtener departamento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<DepartamentoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(departamentoService.findById(id));
    }

    @Operation(summary = "Crear nuevo departamento")
    @PostMapping
    public ResponseEntity<DepartamentoResponseDTO> create(@Valid @RequestBody DepartamentoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departamentoService.create(request));
    }

    @Operation(summary = "Actualizar departamento")
    @PutMapping("/{id}")
    public ResponseEntity<DepartamentoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody DepartamentoRequestDTO request) {
        return ResponseEntity.ok(departamentoService.update(id, request));
    }

    @Operation(summary = "Eliminar departamento")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        departamentoService.delete(id);
    }
}
