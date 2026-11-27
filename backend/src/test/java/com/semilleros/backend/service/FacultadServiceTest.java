package com.semilleros.backend.service;

import com.semilleros.backend.dto.FacultadRequestDTO;
import com.semilleros.backend.dto.FacultadResponseDTO;
import com.semilleros.backend.entity.Facultad;
import com.semilleros.backend.entity.Persona;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
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
class FacultadServiceTest {

    @Mock
    private FacultadRepository facultadRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private FacultadService facultadService;

    @Test
    void create_shouldSaveAndReturnFacultad_whenDataIsValid() {
        FacultadRequestDTO request = new FacultadRequestDTO("Facultad de Ingeniería", "FING", null);
        Facultad saved = Facultad.builder().id(1L).nombre("Facultad de Ingeniería").sigla("FING").build();

        when(facultadRepository.existsByNombreIgnoreCase("Facultad de Ingeniería")).thenReturn(false);
        when(facultadRepository.existsBySiglaIgnoreCase("FING")).thenReturn(false);
        when(facultadRepository.save(any(Facultad.class))).thenReturn(saved);

        FacultadResponseDTO response = facultadService.create(request);

        assertNotNull(response);
        assertEquals("Facultad de Ingeniería", response.getNombre());
        assertEquals("FING", response.getSigla());
        verify(facultadRepository).save(any(Facultad.class));
    }

    @Test
    void create_shouldThrowBusinessException_whenNombreExists() {
        FacultadRequestDTO request = new FacultadRequestDTO("Facultad de Ingeniería", "FING", null);
        when(facultadRepository.existsByNombreIgnoreCase("Facultad de Ingeniería")).thenReturn(true);

        assertThrows(BusinessException.class, () -> facultadService.create(request));
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(facultadRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> facultadService.findById(999L));
    }
}
