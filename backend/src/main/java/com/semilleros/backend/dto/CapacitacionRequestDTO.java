package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de entrada para registrar o actualizar una capacitación de un semillero")
public class CapacitacionRequestDTO {

    @NotNull(message = "El ID del semillero es obligatorio")
    @Schema(description = "Identificador del semillero responsable", example = "1")
    private Long semilleroId;

    @NotBlank(message = "El nombre de la capacitación es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
    @Schema(description = "Nombre descriptivo de la capacitación", example = "Taller de Redes Neuronales y Deep Learning con PyTorch")
    private String nombreCapacitacion;

    @NotNull(message = "La fecha de la capacitación es obligatoria")
    @Schema(description = "Fecha de realización de la capacitación", example = "2024-03-20")
    private LocalDate fecha;

    @NotBlank(message = "El objetivo de la capacitación es obligatorio")
    @Schema(description = "Objetivo principal de la capacitación", example = "Capacitar a los semilleristas en el diseño y entrenamiento de redes neuronales.")
    private String objetivoCapacitacion;

    @NotNull(message = "El porcentaje de cumplimiento es obligatorio")
    @Min(value = 0, message = "El porcentaje de cumplimiento no puede ser menor a 0")
    @Max(value = 100, message = "El porcentaje de cumplimiento no puede ser mayor a 100")
    @Schema(description = "Porcentaje de cumplimiento de la capacitación (0 - 100%)", example = "100.0")
    private Double porcentajeCumplimiento;

    @NotNull(message = "La cantidad de participantes es obligatoria")
    @PositiveOrZero(message = "La cantidad de participantes debe ser mayor o igual a 0")
    @Schema(description = "Cantidad de participantes en la capacitación", example = "28")
    private Integer cantidadParticipantes;
}
