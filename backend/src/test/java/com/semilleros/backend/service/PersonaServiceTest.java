package com.semilleros.backend.service;

import com.semilleros.backend.dto.PersonaRequestDTO;
import com.semilleros.backend.dto.PersonaResponseDTO;
import com.semilleros.backend.entity.Persona;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.DepartamentoRepository;
import com.semilleros.backend.repository.PersonaRepository;
import com.semilleros.backend.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private DepartamentoRepository departamentoRepository;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private PersonaService personaService;

    @Test
    void create_shouldSaveAndReturnPersona_whenDataIsValid() {
        PersonaRequestDTO request = PersonaRequestDTO.builder()
                .identificacion("10901001")
                .tipoDocumento("CC")
                .nombres("Ana María")
                .apellidos("Pérez")
                .correo("ana.perez@universidad.edu.co")
                .cvlacUrl("https://scienti.minciencias.gov.co/cvlac/10901001")
                .build();

        Persona saved = Persona.builder()
                .id(1L)
                .identificacion("10901001")
                .tipoDocumento("CC")
                .nombres("Ana María")
                .apellidos("Pérez")
                .correo("ana.perez@universidad.edu.co")
                .cvlacUrl("https://scienti.minciencias.gov.co/cvlac/10901001")
                .activo(true)
                .build();

        when(personaRepository.existsByIdentificacion("10901001")).thenReturn(false);
        when(personaRepository.existsByCorreoIgnoreCase("ana.perez@universidad.edu.co")).thenReturn(false);
        when(personaRepository.save(any(Persona.class))).thenReturn(saved);

        PersonaResponseDTO response = personaService.create(request);

        assertNotNull(response);
        assertEquals("10901001", response.getIdentificacion());
        assertEquals("Ana María Pérez", response.getNombreCompleto());
        assertEquals("https://scienti.minciencias.gov.co/cvlac/10901001", response.getCvlacUrl());
        verify(personaRepository).save(any(Persona.class));
    }

    @Test
    void create_shouldThrowBusinessException_whenIdentificacionExists() {
        PersonaRequestDTO request = PersonaRequestDTO.builder()
                .identificacion("10901001")
                .correo("ana.perez@universidad.edu.co")
                .build();

        when(personaRepository.existsByIdentificacion("10901001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> personaService.create(request));
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(personaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> personaService.findById(999L));
    }
}
