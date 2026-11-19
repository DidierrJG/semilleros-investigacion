package com.semilleros.backend.controller;

import com.semilleros.backend.dto.GrupoInvestigacionRequestDTO;
import com.semilleros.backend.dto.GrupoInvestigacionResponseDTO;
import com.semilleros.backend.service.GrupoInvestigacionService;
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
@RequestMapping("/api/grupos-investigacion")
@RequiredArgsConstructor
@Tag(name = "Grupos de Investigación", description = "Operaciones CRUD para la gestión de grupos de investigación de la UFPS")
public class GrupoInvestigacionController {

    private final GrupoInvestigacionService grupoInvestigacionService;

    @GetMapping
    @Operation(summary = "Listar grupos de investigación", description = "Retorna todos los grupos de investigación de la UFPS registrados")
    public ResponseEntity<List<GrupoInvestigacionResponseDTO>> findAll() {
        return ResponseEntity.ok(grupoInvestigacionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener grupo de investigación por ID", description = "Retorna un grupo de investigación según su identificador")
    public ResponseEntity<GrupoInvestigacionResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(grupoInvestigacionService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear grupo de investigación", description = "Registra un nuevo grupo de investigación de la UFPS validando sigla, código Minciencias y correo únicos")
    public ResponseEntity<GrupoInvestigacionResponseDTO> create(@Valid @RequestBody GrupoInvestigacionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoInvestigacionService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar grupo de investigación", description = "Actualiza los datos de un grupo de investigación existente")
    public ResponseEntity<GrupoInvestigacionResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody GrupoInvestigacionRequestDTO request) {
        return ResponseEntity.ok(grupoInvestigacionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar grupo de investigación", description = "Elimina un grupo de investigación si no tiene semilleros asociados")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        grupoInvestigacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
