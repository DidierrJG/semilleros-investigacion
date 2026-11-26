package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de respuesta de un rol institucional")
public class RolResponseDTO {

    @Schema(description = "Identificador del rol", example = "1")
    private Long id;

    @Schema(description = "Código único del rol", example = "DIRECTOR_DEPARTAMENTO")
    private String codigo;

    @Schema(description = "Nombre legible del rol", example = "Director de Departamento")
    private String nombre;

    @Schema(description = "Descripción de las responsabilidades del rol")
    private String descripcion;
}
