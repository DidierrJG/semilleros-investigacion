package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para registrar o actualizar un departamento")
public class DepartamentoRequestDTO {

    @NotBlank(message = "El nombre del departamento es obligatorio")
    @Size(max = 180, message = "El nombre no puede superar 180 caracteres")
    @Schema(description = "Nombre del departamento", example = "Sistemas e Informática")
    private String nombre;

    @NotNull(message = "La facultad es obligatoria")
    @Schema(description = "ID de la facultad a la que pertenece el departamento", example = "1")
    private Long facultadId;

    @Schema(description = "ID de la persona que es director del departamento", example = "1")
    private Long directorId;
}
