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
@Schema(description = "Datos de entrada para crear o actualizar un programa académico")
public class ProgramaAcademicoRequestDTO {

    @NotBlank(message = "El nombre del programa es obligatorio")
    @Size(max = 160, message = "El nombre no puede superar los 160 caracteres")
    @Schema(description = "Nombre del programa académico", example = "Ingeniería de Sistemas")
    private String nombre;

    @Schema(description = "ID de la facultad a la que pertenece el programa", example = "1")
    private Long facultadId;
}
