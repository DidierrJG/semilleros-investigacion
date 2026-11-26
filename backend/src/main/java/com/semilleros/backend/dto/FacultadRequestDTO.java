package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para crear o actualizar una facultad")
public class FacultadRequestDTO {

    @NotBlank(message = "El nombre de la facultad es obligatorio")
    @Size(max = 180, message = "El nombre no puede superar 180 caracteres")
    @Schema(description = "Nombre oficial de la facultad", example = "Facultad de Ingeniería")
    private String nombre;

    @NotBlank(message = "La sigla de la facultad es obligatoria")
    @Size(max = 20, message = "La sigla no puede superar 20 caracteres")
    @Schema(description = "Sigla institucional", example = "FING")
    private String sigla;

    @Schema(description = "ID de la persona representante de la facultad ante investigación", example = "1")
    private Long representanteId;
}
