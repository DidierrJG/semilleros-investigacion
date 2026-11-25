package com.semilleros.backend.service;

import com.semilleros.backend.entity.Actividad;
import com.semilleros.backend.entity.Capacitacion;
import com.semilleros.backend.entity.Director;
import com.semilleros.backend.entity.GrupoInvestigacion;
import com.semilleros.backend.entity.Integrante;
import com.semilleros.backend.entity.Producto;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.entity.TipoActividad;
import com.semilleros.backend.entity.TipoProducto;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.ActividadRepository;
import com.semilleros.backend.repository.CapacitacionRepository;
import com.semilleros.backend.repository.IntegranteRepository;
import com.semilleros.backend.repository.ProductoRepository;
import com.semilleros.backend.repository.SemilleroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteFoIn14ServiceTest {

    @Mock
    private SemilleroRepository semilleroRepository;

    @Mock
    private CapacitacionRepository capacitacionRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private IntegranteRepository integranteRepository;

    @InjectMocks
    private ReporteFoIn14Service reporteService;

    private Semillero semilleroMock;

    @BeforeEach
    void setUp() {
        Director director = Director.builder()
                .id(1L)
                .nombre("Dra. Claudia Gómez")
                .correo("claudia.gomez@ufps.edu.co")
                .build();

        GrupoInvestigacion grupo = GrupoInvestigacion.builder()
                .id(1L)
                .nombre("Grupo de Sistemas Inteligentes")
                .sigla("GSI")
                .codigoMinciencias("COL0021481")
                .categoria("A")
                .facultad("Facultad de Ingeniería")
                .departamento("Sistemas e Informática")
                .lider("Dra. Ana María Pérez")
                .correo("gsi@ufps.edu.co")
                .build();

        semilleroMock = Semillero.builder()
                .id(1L)
                .nombre("Semillero de Inteligencia Artificial Aplicada")
                .sigla("SIAA")
                .director(director)
                .grupoInvestigacion(grupo)
                .build();
    }

    @Test
    @DisplayName("Debe generar exitosamente el PDF del reporte FO-IN-14 con datos reales")
    void generarReporteFoIn14_shouldGenerateValidPdf_whenDataPresent() {
        // Arrange
        Capacitacion cap = Capacitacion.builder()
                .id(1L)
                .semillero(semilleroMock)
                .nombreCapacitacion("Taller de PyTorch")
                .objetivoCapacitacion("Diseñar redes neuronales")
                .fecha(LocalDate.of(2024, 3, 20))
                .porcentajeCumplimiento(100.0)
                .cantidadParticipantes(28)
                .build();

        TipoActividad tipoAct = TipoActividad.builder().id(1L).nombre("Taller formativo").build();
        Actividad act = Actividad.builder()
                .id(1L)
                .semillero(semilleroMock)
                .tipoActividad(tipoAct)
                .nombre("Seminario de Deep Learning")
                .porcentajeCumplimiento(90)
                .build();

        Integrante integrante = Integrante.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .codigo("1151234")
                .build();

        Set<Integrante> integrantesSet = new HashSet<>();
        integrantesSet.add(integrante);

        Producto prod = Producto.builder()
                .id(1L)
                .semillero(semilleroMock)
                .nombre("Asistente Virtual con RAG")
                .descripcion("Plataforma basada en LLMs")
                .tipo(TipoProducto.SOFTWARE)
                .integrantes(integrantesSet)
                .build();

        when(semilleroRepository.findById(1L)).thenReturn(Optional.of(semilleroMock));
        when(capacitacionRepository.findBySemilleroId(1L)).thenReturn(List.of(cap));
        when(actividadRepository.findBySemilleroId(1L)).thenReturn(List.of(act));
        when(productoRepository.findBySemilleroId(1L)).thenReturn(List.of(prod));
        when(integranteRepository.findBySemilleroId(1L)).thenReturn(List.of(integrante));

        // Act
        byte[] pdfBytes = reporteService.generarReporteFoIn14(1L, "2024-1");

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "El PDF generado debe tener un tamaño válido");
        String header = new String(pdfBytes, 0, Math.min(10, pdfBytes.length), StandardCharsets.US_ASCII);
        assertTrue(header.startsWith("%PDF-"), "El archivo debe iniciar con la cabecera estándar de PDF (%PDF-)");
    }

    @Test
    @DisplayName("Debe generar el PDF sin errores aún si el semillero no tiene capacitaciones ni productos")
    void generarReporteFoIn14_shouldGenerateValidPdf_whenEmptyCollections() {
        when(semilleroRepository.findById(1L)).thenReturn(Optional.of(semilleroMock));
        when(capacitacionRepository.findBySemilleroId(1L)).thenReturn(Collections.emptyList());
        when(actividadRepository.findBySemilleroId(1L)).thenReturn(Collections.emptyList());
        when(productoRepository.findBySemilleroId(1L)).thenReturn(Collections.emptyList());
        when(integranteRepository.findBySemilleroId(1L)).thenReturn(Collections.emptyList());

        byte[] pdfBytes = reporteService.generarReporteFoIn14(1L, "2024-1");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
        String header = new String(pdfBytes, 0, 10, StandardCharsets.US_ASCII);
        assertTrue(header.startsWith("%PDF-"));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el semillero no existe")
    void generarReporteFoIn14_shouldThrowException_whenSemilleroNotFound() {
        when(semilleroRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                reporteService.generarReporteFoIn14(999L, "2024-1"));
    }
}
