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
@Schema(description = "Resumen básico de persona para relaciones y listados")
public class PersonaSummaryDTO {

    @Schema(description = "Identificador de la persona", example = "1")
    private Long id;

    @Schema(description = "Número de identificación", example = "10901001")
    private String identificacion;

    @Schema(description = "Nombre completo", example = "Dra. Ana María Pérez")
    private String nombreCompleto;

    @Schema(description = "Correo institucional", example = "ana.perez@universidad.edu.co")
    private String correo;
}
