package com.semilleros.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;


@Schema(description = "Datos para crear o actualizar una Capacitación")
public class CapacitacionRequestDTO {

    @NotNull
    @jakarta.validation.constraints.Positive
    @Schema(description = "ID del semillero que realiza la capacitación", example = "1")
    private Long semilleroId;

    @NotBlank
    @Size(max = 200)
    @Schema(description = "Nombre de la capacitación", example = "Capacitación en IA")
    private String nombreCapacitacion;

    @NotNull
    @Schema(description = "Fecha de la capacitación", example = "2024-09-15")
    private LocalDate fecha;

    @NotBlank
    @Schema(description = "Objetivo de la capacitación", example = "Aprender conceptos básicos de IA")
    private String objetivoCapacitacion;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Porcentaje de cumplimiento", example = "85.5")
    private Double porcentajeCumplimiento;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Cantidad de participantes", example = "30")
    private Integer cantidadParticipantes;

    // Getters and Setters
    public String getNombreCapacitacion() { return nombreCapacitacion; }
    public void setNombreCapacitacion(String nombreCapacitacion) { this.nombreCapacitacion = nombreCapacitacion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getObjetivoCapacitacion() { return objetivoCapacitacion; }
    public void setObjetivoCapacitacion(String objetivoCapacitacion) { this.objetivoCapacitacion = objetivoCapacitacion; }
    public Double getPorcentajeCumplimiento() { return porcentajeCumplimiento; }
    public void setPorcentajeCumplimiento(Double porcentajeCumplimiento) { this.porcentajeCumplimiento = porcentajeCumplimiento; }
    public Integer getCantidadParticipantes() { return cantidadParticipantes; }
    public void setCantidadParticipantes(Integer cantidadParticipantes) { this.cantidadParticipantes = cantidadParticipantes; }
}

