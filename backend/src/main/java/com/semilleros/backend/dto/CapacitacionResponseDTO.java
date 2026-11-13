package com.semilleros.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Datos de salida de una Capacitación")
public class CapacitacionResponseDTO {

    @Schema(description = "ID de la capacitación", example = "1")
    private Long id;

    @Schema(description = "Nombre de la capacitación", example = "Capacitación en IA")
    private String nombreCapacitacion;

    @Schema(description = "Fecha de la capacitación", example = "2024-09-15")
    private LocalDate fecha;

    @Schema(description = "Objetivo de la capacitación", example = "Aprender conceptos básicos de IA")
    private String objetivoCapacitacion;

    @Schema(description = "Porcentaje de cumplimiento", example = "85.5")
    private Double porcentajeCumplimiento;

    @Schema(description = "Cantidad de participantes", example = "30")
    private Integer cantidadParticipantes;

    @Schema(description = "ID del semillero que realizó la capacitación", example = "2")
    private Long semilleroId;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Long getSemilleroId() { return semilleroId; }
    public void setSemilleroId(Long semilleroId) { this.semilleroId = semilleroId; }
}

}

