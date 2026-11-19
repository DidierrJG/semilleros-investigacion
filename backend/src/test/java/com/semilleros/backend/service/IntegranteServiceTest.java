package com.semilleros.backend.service;

import com.semilleros.backend.dto.IntegranteRequestDTO;
import com.semilleros.backend.dto.IntegranteResponseDTO;
import com.semilleros.backend.entity.Integrante;
import com.semilleros.backend.entity.ProgramaAcademico;
import com.semilleros.backend.entity.Semillero;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.repository.IntegranteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegranteServiceTest {

    @Mock
    private IntegranteRepository integranteRepository;

    @Mock
    private SemilleroService semilleroService;

    @Mock
    private ProgramaAcademicoService programaService;

    @InjectMocks
    private IntegranteService integranteService;

    @Test
    void create_shouldSaveAndReturnIntegrante_whenValid() {
        IntegranteRequestDTO request = IntegranteRequestDTO.builder()
                .semilleroId(10L)
                .programaId(2L)
                .codigo("20241001")
                .nombre("Carlos Gómez")
                .esLider(true)
                .identificacion("1020304050")
                .semestreActual(4)
                .edad(20)
                .direccion("Calle 123 # 45-67, Bogotá")
                .email("carlos.gomez@universidad.edu.co")
                .celular("3001234567")
                .build();

        Semillero semillero = Semillero.builder().id(10L).nombre("Semillero IA").sigla("SIA").build();
        ProgramaAcademico programa = ProgramaAcademico.builder().id(2L).nombre("Ingeniería de Sistemas").build();
        Integrante saved = Integrante.builder()
                .id(1L)
                .semillero(semillero)
                .programa(programa)
                .codigo("20241001")
                .nombre("Carlos Gómez")
                .esLider(true)
                .identificacion("1020304050")
                .semestreActual(4)
                .edad(20)
                .direccion("Calle 123 # 45-67, Bogotá")
                .email("carlos.gomez@universidad.edu.co")
                .celular("3001234567")
                .build();

        when(semilleroService.findEntityById(10L)).thenReturn(semillero);
        when(programaService.findEntityById(2L)).thenReturn(programa);
        when(integranteRepository.existsBySemilleroIdAndCodigoIgnoreCase(10L, "20241001")).thenReturn(false);
        when(integranteRepository.save(any(Integrante.class))).thenReturn(saved);

        IntegranteResponseDTO result = integranteService.create(request);

        assertEquals(1L, result.getId());
        assertEquals(10L, result.getSemilleroId());
        assertEquals("20241001", result.getCodigo());
        assertEquals(true, result.getEsLider());
        assertEquals(2L, result.getProgramaId());
        assertEquals("Ingeniería de Sistemas", result.getProgramaNombre());
    }

    @Test
    void create_shouldThrowBusinessException_whenCodigoDuplicatedInSemillero() {
        IntegranteRequestDTO request = IntegranteRequestDTO.builder()
                .semilleroId(10L)
                .programaId(2L)
                .codigo("20241001")
                .nombre("Carlos Gómez")
                .esLider(false)
                .identificacion("1020304050")
                .semestreActual(4)
                .edad(20)
                .direccion("Calle 123 # 45-67, Bogotá")
                .email("carlos.gomez@universidad.edu.co")
                .celular("3001234567")
                .build();

        Semillero semillero = Semillero.builder().id(10L).nombre("Semillero IA").sigla("SIA").build();
        ProgramaAcademico programa = ProgramaAcademico.builder().id(2L).nombre("Ingeniería de Sistemas").build();
        when(semilleroService.findEntityById(10L)).thenReturn(semillero);
        when(programaService.findEntityById(2L)).thenReturn(programa);
        when(integranteRepository.existsBySemilleroIdAndCodigoIgnoreCase(10L, "20241001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> integranteService.create(request));
    }
}

