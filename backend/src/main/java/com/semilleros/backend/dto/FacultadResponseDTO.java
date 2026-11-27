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
@Schema(description = "Datos de respuesta de una facultad")
public class FacultadResponseDTO {

    @Schema(description = "Identificador de la facultad", example = "1")
    private Long id;

    @Schema(description = "Nombre de la facultad", example = "Facultad de Ingeniería")
    private String nombre;

    @Schema(description = "Sigla de la facultad", example = "FING")
    private String sigla;

    @Schema(description = "Representante de la facultad ante investigación")
    private PersonaSummaryDTO representante;
}
