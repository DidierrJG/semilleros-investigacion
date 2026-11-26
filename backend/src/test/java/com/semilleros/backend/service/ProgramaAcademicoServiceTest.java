package com.semilleros.backend.service;

import com.semilleros.backend.dto.ProgramaAcademicoRequestDTO;
import com.semilleros.backend.dto.ProgramaAcademicoResponseDTO;
import com.semilleros.backend.entity.Facultad;
import com.semilleros.backend.entity.ProgramaAcademico;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.FacultadRepository;
import com.semilleros.backend.repository.IntegranteRepository;
import com.semilleros.backend.repository.ProgramaAcademicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgramaAcademicoServiceTest {

    @Mock
    private ProgramaAcademicoRepository programaRepository;

    @Mock
    private IntegranteRepository integranteRepository;

    @Mock
    private FacultadRepository facultadRepository;

    @InjectMocks
    private ProgramaAcademicoService programaService;

    @Test
    void create_shouldSaveAndReturnPrograma_whenNombreIsUnique() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Facultad de Ingeniería").sigla("FING").build();
        ProgramaAcademicoRequestDTO request = new ProgramaAcademicoRequestDTO("Ingeniería de Sistemas", 1L);
        ProgramaAcademico saved = ProgramaAcademico.builder().id(1L).nombre("Ingeniería de Sistemas").facultad(facultad).build();

        when(programaRepository.existsByNombreIgnoreCase("Ingeniería de Sistemas")).thenReturn(false);
        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(programaRepository.save(any(ProgramaAcademico.class))).thenReturn(saved);

        ProgramaAcademicoResponseDTO response = programaService.create(request);

        assertNotNull(response);
        assertEquals("Ingeniería de Sistemas", response.getNombre());
        assertEquals("FING", response.getFacultad().getSigla());
        verify(programaRepository).save(any(ProgramaAcademico.class));
    }

    @Test
    void create_shouldThrowBusinessException_whenNombreExists() {
        ProgramaAcademicoRequestDTO request = new ProgramaAcademicoRequestDTO("Ingeniería de Sistemas", null);
        when(programaRepository.existsByNombreIgnoreCase("Ingeniería de Sistemas")).thenReturn(true);

        assertThrows(BusinessException.class, () -> programaService.create(request));
    }

    @Test
    void delete_shouldThrowBusinessException_whenHasIntegrantes() {
        ProgramaAcademico programa = ProgramaAcademico.builder().id(1L).nombre("Ingeniería de Sistemas").build();
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programa));
        when(integranteRepository.existsByProgramaId(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> programaService.delete(1L));
    }
}
