package com.semilleros.backend.service;

import com.semilleros.backend.dto.DirectorResponseDTO;
import com.semilleros.backend.dto.GrupoInvestigacionResponseDTO;
import com.semilleros.backend.dto.LineaResponseDTO;
import com.semilleros.backend.dto.SemilleroRequestDTO;
import com.semilleros.backend.dto.SemilleroResponseDTO;
import com.semilleros.backend.entity.Director;
import com.semilleros.backend.entity.GrupoInvestigacion;
import com.semilleros.backend.entity.Linea;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.repository.ActividadRepository;
import com.semilleros.backend.repository.IntegranteRepository;
import com.semilleros.backend.repository.ProductoRepository;
import com.semilleros.backend.repository.SemilleroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemilleroServiceTest {

    @Mock
    private SemilleroRepository semilleroRepository;

    @Mock
    private IntegranteRepository integranteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private DirectorService directorService;

    @Mock
    private LineaService lineaService;

    @Mock
    private GrupoInvestigacionService grupoInvestigacionService;

    @InjectMocks
    private SemilleroService semilleroService;

    @Test
    void create_shouldSaveAndReturnSemilleroWithLineas_whenValid() {
        SemilleroRequestDTO request = SemilleroRequestDTO.builder()
                .nombre("Semillero IA")
                .sigla("SIA")
                .grupoInvestigacionId(2L)
                .mision("Mision")
                .vision("Vision")
                .objetivoGeneral("Objetivo")
                .directorId(1L)
                .lineaIds(Set.of(5L))
                .build();

        Director director = Director.builder().id(1L).nombre("Ana").correo("ana@uni.edu").build();
        DirectorResponseDTO directorDTO = new DirectorResponseDTO(1L, "Ana", "ana@uni.edu");
        GrupoInvestigacion grupo = GrupoInvestigacion.builder().id(2L).nombre("GSI").sigla("GSI").build();
        GrupoInvestigacionResponseDTO grupoDTO = GrupoInvestigacionResponseDTO.builder().id(2L).nombre("GSI").sigla("GSI").build();
        Linea linea = Linea.builder().id(5L).nombre("Machine Learning").build();
        LineaResponseDTO lineaDTO = new LineaResponseDTO(5L, "Machine Learning");

        Semillero saved = Semillero.builder()
                .id(10L)
                .nombre("Semillero IA")
                .sigla("SIA")
                .grupoInvestigacion(grupo)
                .mision("Mision")
                .vision("Vision")
                .objetivoGeneral("Objetivo")
                .director(director)
                .lineas(Set.of(linea))
                .build();

        when(semilleroRepository.existsBySiglaIgnoreCase("SIA")).thenReturn(false);
        when(directorService.findEntityById(1L)).thenReturn(director);
        when(grupoInvestigacionService.findEntityById(2L)).thenReturn(grupo);
        when(lineaService.findEntityById(5L)).thenReturn(linea);
        when(semilleroRepository.save(any(Semillero.class))).thenReturn(saved);
        when(directorService.toResponseDTO(director)).thenReturn(directorDTO);
        when(grupoInvestigacionService.toResponseDTO(grupo)).thenReturn(grupoDTO);
        when(lineaService.toResponseDTO(linea)).thenReturn(lineaDTO);

        SemilleroResponseDTO result = semilleroService.create(request);

        assertEquals(10L, result.getId());
        assertEquals("SIA", result.getSigla());
        assertEquals(1L, result.getDirector().getId());
        assertEquals(2L, result.getGrupoInvestigacion().getId());
        assertEquals(1, result.getLineas().size());
        assertEquals(5L, result.getLineas().get(0).getId());
    }

    @Test
    void create_shouldThrowBusinessException_whenSiglaAlreadyExists() {
        SemilleroRequestDTO request = SemilleroRequestDTO.builder()
                .nombre("Semillero IA")
                .sigla("SIA")
                .grupoInvestigacionId(2L)
                .directorId(1L)
                .build();

        when(semilleroRepository.existsBySiglaIgnoreCase("SIA")).thenReturn(true);

        assertThrows(BusinessException.class, () -> semilleroService.create(request));
    }

    @Test
    void delete_shouldThrowBusinessException_whenSemilleroHasIntegrantes() {
        Semillero semillero = Semillero.builder().id(10L).nombre("Semillero IA").sigla("SIA").build();
        when(semilleroRepository.findById(10L)).thenReturn(Optional.of(semillero));
        when(integranteRepository.existsBySemilleroId(10L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> semilleroService.delete(10L));
    }
}
