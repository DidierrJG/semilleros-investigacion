package com.semilleros.backend.service;

import com.semilleros.backend.dto.GrupoInvestigacionRequestDTO;
import com.semilleros.backend.dto.GrupoInvestigacionResponseDTO;
import com.semilleros.backend.entity.GrupoInvestigacion;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.GrupoInvestigacionRepository;
import com.semilleros.backend.repository.SemilleroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrupoInvestigacionServiceTest {

    @Mock
    private GrupoInvestigacionRepository grupoInvestigacionRepository;

    @Mock
    private SemilleroRepository semilleroRepository;

    @InjectMocks
    private GrupoInvestigacionService grupoInvestigacionService;

    @Test
    void create_shouldSaveAndReturnGrupo_whenValid() {
        GrupoInvestigacionRequestDTO request = GrupoInvestigacionRequestDTO.builder()
                .nombre("Grupo de Ingeniería de Software")
                .sigla("GIDISOFT")
                .codigoMinciencias("COL0006204")
                .categoria("A1")
                .facultad("Facultad de Ingeniería")
                .departamento("Sistemas e Informática")
                .lider("Dr. Carlos Martínez")
                .correo("gidisoft@ufps.edu.co")
                .sitioWeb("https://ufps.edu.co")
                .build();

        GrupoInvestigacion saved = GrupoInvestigacion.builder()
                .id(1L)
                .nombre("Grupo de Ingeniería de Software")
                .sigla("GIDISOFT")
                .codigoMinciencias("COL0006204")
                .categoria("A1")
                .facultad("Facultad de Ingeniería")
                .departamento("Sistemas e Informática")
                .lider("Dr. Carlos Martínez")
                .correo("gidisoft@ufps.edu.co")
                .sitioWeb("https://ufps.edu.co")
                .build();

        when(grupoInvestigacionRepository.existsBySiglaIgnoreCase("GIDISOFT")).thenReturn(false);
        when(grupoInvestigacionRepository.existsByCodigoMincienciasIgnoreCase("COL0006204")).thenReturn(false);
        when(grupoInvestigacionRepository.existsByCorreoIgnoreCase("gidisoft@ufps.edu.co")).thenReturn(false);
        when(grupoInvestigacionRepository.save(any(GrupoInvestigacion.class))).thenReturn(saved);

        GrupoInvestigacionResponseDTO response = grupoInvestigacionService.create(request);

        assertEquals(1L, response.getId());
        assertEquals("GIDISOFT", response.getSigla());
        assertEquals("COL0006204", response.getCodigoMinciencias());
        assertEquals("A1", response.getCategoria());
        assertEquals("Facultad de Ingeniería", response.getFacultad());
    }

    @Test
    void create_shouldThrowBusinessException_whenSiglaExists() {
        GrupoInvestigacionRequestDTO request = GrupoInvestigacionRequestDTO.builder()
                .sigla("GIDISOFT")
                .build();

        when(grupoInvestigacionRepository.existsBySiglaIgnoreCase("GIDISOFT")).thenReturn(true);

        assertThrows(BusinessException.class, () -> grupoInvestigacionService.create(request));
    }

    @Test
    void create_shouldThrowBusinessException_whenCodigoMincienciasExists() {
        GrupoInvestigacionRequestDTO request = GrupoInvestigacionRequestDTO.builder()
                .sigla("GIDISOFT")
                .codigoMinciencias("COL0006204")
                .build();

        when(grupoInvestigacionRepository.existsBySiglaIgnoreCase("GIDISOFT")).thenReturn(false);
        when(grupoInvestigacionRepository.existsByCodigoMincienciasIgnoreCase("COL0006204")).thenReturn(true);

        assertThrows(BusinessException.class, () -> grupoInvestigacionService.create(request));
    }

    @Test
    void create_shouldThrowBusinessException_whenCorreoExists() {
        GrupoInvestigacionRequestDTO request = GrupoInvestigacionRequestDTO.builder()
                .sigla("GIDISOFT")
                .codigoMinciencias("COL0006204")
                .correo("gidisoft@ufps.edu.co")
                .build();

        when(grupoInvestigacionRepository.existsBySiglaIgnoreCase("GIDISOFT")).thenReturn(false);
        when(grupoInvestigacionRepository.existsByCodigoMincienciasIgnoreCase("COL0006204")).thenReturn(false);
        when(grupoInvestigacionRepository.existsByCorreoIgnoreCase("gidisoft@ufps.edu.co")).thenReturn(true);

        assertThrows(BusinessException.class, () -> grupoInvestigacionService.create(request));
    }

    @Test
    void findById_shouldThrowResourceNotFound_whenMissing() {
        when(grupoInvestigacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> grupoInvestigacionService.findById(99L));
    }

    @Test
    void delete_shouldThrowBusinessException_whenHasSemilleros() {
        GrupoInvestigacion grupo = GrupoInvestigacion.builder().id(1L).sigla("GIDISOFT").build();
        when(grupoInvestigacionRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(semilleroRepository.existsByGrupoInvestigacionId(1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> grupoInvestigacionService.delete(1L));
    }

    @Test
    void delete_shouldDeleteGrupo_whenNoSemillerosAssociated() {
        GrupoInvestigacion grupo = GrupoInvestigacion.builder().id(1L).sigla("GIDISOFT").build();
        when(grupoInvestigacionRepository.findById(1L)).thenReturn(Optional.of(grupo));
        when(semilleroRepository.existsByGrupoInvestigacionId(1L)).thenReturn(false);

        grupoInvestigacionService.delete(1L);

        verify(grupoInvestigacionRepository).delete(grupo);
    }
}
