package com.semilleros.backend.controller;

import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.SemilleroRepository;
import com.semilleros.backend.service.ReporteFoIn14Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reportes Institucionales", description = "Generación y descarga de formatos institucionales de investigación en formato PDF")
public class ReporteController {

    private final ReporteFoIn14Service reporteFoIn14Service;
    private final SemilleroRepository semilleroRepository;

    @Operation(
            summary = "Descargar reporte FO-IN-14 (Informe de Gestión de Semilleros de Investigación)",
            description = "Genera dinámicamente y descarga el archivo PDF del formato institucional FO-IN-14 con base en los datos reales del semillero.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Documento PDF generado exitosamente",
                            content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))
                    ),
                    @ApiResponse(responseCode = "404", description = "Semillero no encontrado")
            }
    )
    @GetMapping(value = "/semilleros/{id}/fo-in-14", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarReporteFoIn14(
            @Parameter(description = "ID del semillero", required = true) @PathVariable Long id,
            @Parameter(description = "Periodo académico (ej. 2024-1, 2025-1)") @RequestParam(required = false) String periodo
    ) {
        Semillero semillero = semilleroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semillero no encontrado con ID: " + id));

        String periodoFinal = (periodo != null && !periodo.isBlank())
                ? periodo.trim()
                : String.format("%d-%d", LocalDate.now().getYear(), LocalDate.now().getMonthValue() <= 6 ? 1 : 2);

        byte[] pdfBytes = reporteFoIn14Service.generarReporteFoIn14(id, periodoFinal);

        String siglaSanitizada = (semillero.getSigla() != null)
                ? semillero.getSigla().replaceAll("[^a-zA-Z0-9_-]", "_")
                : "SEMILLERO";
        String filename = String.format("FO-IN-14_%s_%s.pdf", siglaSanitizada, periodoFinal);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
