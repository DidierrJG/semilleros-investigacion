package com.semilleros.backend.controller;

import com.semilleros.backend.dto.PersonaRequestDTO;
import com.semilleros.backend.dto.PersonaResponseDTO;
import com.semilleros.backend.dto.RolResponseDTO;
import com.semilleros.backend.service.PersonaService;
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
@RequestMapping("/api/personas")
@RequiredArgsConstructor
@Tag(name = "Personas", description = "Gestión centralizada de docentes, directores, representantes y roles institucionales")
public class PersonaController {

    private final PersonaService personaService;

    @Operation(summary = "Listar personas (opcionalmente filtradas por departamento o por rol)")
    @GetMapping
    public ResponseEntity<List<PersonaResponseDTO>> findAll(
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String rol
    ) {
        if (departamentoId != null) {
            return ResponseEntity.ok(personaService.findByDepartamentoId(departamentoId));
        }
        if (rol != null && !rol.isBlank()) {
            return ResponseEntity.ok(personaService.findByRol(rol.trim().toUpperCase()));
        }
        return ResponseEntity.ok(personaService.findAll());
    }

    @Operation(summary = "Listar roles institucionales disponibles en el catálogo")
    @GetMapping("/roles")
    public ResponseEntity<List<RolResponseDTO>> findAllRoles() {
        return ResponseEntity.ok(personaService.findAllRoles());
    }

    @Operation(summary = "Obtener persona por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PersonaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.findById(id));
    }

    @Operation(summary = "Crear nueva persona")
    @PostMapping
    public ResponseEntity<PersonaResponseDTO> create(@Valid @RequestBody PersonaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personaService.create(request));
    }

    @Operation(summary = "Actualizar persona existente")
    @PutMapping("/{id}")
    public ResponseEntity<PersonaResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PersonaRequestDTO request) {
        return ResponseEntity.ok(personaService.update(id, request));
    }

    @Operation(summary = "Eliminar persona")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        personaService.delete(id);
    }
}
