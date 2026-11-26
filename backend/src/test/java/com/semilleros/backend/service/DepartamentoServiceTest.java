package com.semilleros.backend.service;

import com.semilleros.backend.dto.DepartamentoRequestDTO;
import com.semilleros.backend.dto.DepartamentoResponseDTO;
import com.semilleros.backend.entity.Departamento;
import com.semilleros.backend.entity.Facultad;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.DepartamentoRepository;
import com.semilleros.backend.repository.FacultadRepository;
import com.semilleros.backend.repository.PersonaRepository;
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
class DepartamentoServiceTest {

    @Mock
    private DepartamentoRepository departamentoRepository;

    @Mock
    private FacultadRepository facultadRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private DepartamentoService departamentoService;

    @Test
    void create_shouldSaveAndReturnDepartamento_whenDataIsValid() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Facultad de Ingeniería").sigla("FING").build();
        DepartamentoRequestDTO request = new DepartamentoRequestDTO("Sistemas e Informática", 1L, null);
        Departamento saved = Departamento.builder().id(1L).nombre("Sistemas e Informática").facultad(facultad).build();

        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(departamentoRepository.existsByNombreIgnoreCaseAndFacultadId("Sistemas e Informática", 1L)).thenReturn(false);
        when(departamentoRepository.save(any(Departamento.class))).thenReturn(saved);

        DepartamentoResponseDTO response = departamentoService.create(request);

        assertNotNull(response);
        assertEquals("Sistemas e Informática", response.getNombre());
        assertEquals("FING", response.getFacultad().getSigla());
        verify(departamentoRepository).save(any(Departamento.class));
    }

    @Test
    void create_shouldThrowBusinessException_whenNombreExistsInSameFacultad() {
        Facultad facultad = Facultad.builder().id(1L).nombre("Facultad de Ingeniería").build();
        DepartamentoRequestDTO request = new DepartamentoRequestDTO("Sistemas e Informática", 1L, null);

        when(facultadRepository.findById(1L)).thenReturn(Optional.of(facultad));
        when(departamentoRepository.existsByNombreIgnoreCaseAndFacultadId("Sistemas e Informática", 1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> departamentoService.create(request));
    }
}
