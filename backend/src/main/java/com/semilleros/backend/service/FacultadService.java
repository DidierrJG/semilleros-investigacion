package com.semilleros.backend.service;

import com.semilleros.backend.dto.FacultadRequestDTO;
import com.semilleros.backend.dto.FacultadResponseDTO;
import com.semilleros.backend.dto.PersonaSummaryDTO;
import com.semilleros.backend.entity.Facultad;
import com.semilleros.backend.entity.Persona;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.FacultadRepository;
import com.semilleros.backend.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultadService {

    private final FacultadRepository facultadRepository;
    private final PersonaRepository personaRepository;

    @Transactional(readOnly = true)
    public List<FacultadResponseDTO> findAll() {
        return facultadRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacultadResponseDTO findById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Facultad findEntityById(Long id) {
        return facultadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facultad no encontrada con ID: " + id));
    }

    @Transactional
    public FacultadResponseDTO create(FacultadRequestDTO request) {
        String nombre = request.getNombre().trim();
        String sigla = request.getSigla().trim().toUpperCase();

        if (facultadRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe una facultad con el nombre: " + nombre);
        }
        if (facultadRepository.existsBySiglaIgnoreCase(sigla)) {
            throw new BusinessException("Ya existe una facultad con la sigla: " + sigla);
        }

        Persona representante = null;
        if (request.getRepresentanteId() != null) {
            representante = personaRepository.findById(request.getRepresentanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada para representante con ID: " + request.getRepresentanteId()));
        }

        Facultad facultad = Facultad.builder()
                .nombre(nombre)
                .sigla(sigla)
                .representante(representante)
                .build();

        return toResponseDTO(facultadRepository.save(facultad));
    }

    @Transactional
    public FacultadResponseDTO update(Long id, FacultadRequestDTO request) {
        Facultad facultad = findEntityById(id);
        String nombre = request.getNombre().trim();
        String sigla = request.getSigla().trim().toUpperCase();

        if (facultadRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new BusinessException("Ya existe otra facultad con el nombre: " + nombre);
        }
        if (facultadRepository.existsBySiglaIgnoreCaseAndIdNot(sigla, id)) {
            throw new BusinessException("Ya existe otra facultad con la sigla: " + sigla);
        }

        Persona representante = null;
        if (request.getRepresentanteId() != null) {
            representante = personaRepository.findById(request.getRepresentanteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada para representante con ID: " + request.getRepresentanteId()));
        }

        facultad.setNombre(nombre);
        facultad.setSigla(sigla);
        facultad.setRepresentante(representante);

        return toResponseDTO(facultadRepository.save(facultad));
    }

    @Transactional
    public void delete(Long id) {
        Facultad facultad = findEntityById(id);
        facultadRepository.delete(facultad);
    }

    public FacultadResponseDTO toResponseDTO(Facultad facultad) {
        PersonaSummaryDTO representanteDTO = null;
        if (facultad.getRepresentante() != null) {
            Persona p = facultad.getRepresentante();
            representanteDTO = PersonaSummaryDTO.builder()
                    .id(p.getId())
                    .identificacion(p.getIdentificacion())
                    .nombreCompleto(p.getNombreCompleto())
                    .correo(p.getCorreo())
                    .build();
        }

        return FacultadResponseDTO.builder()
                .id(facultad.getId())
                .nombre(facultad.getNombre())
                .sigla(facultad.getSigla())
                .representante(representanteDTO)
                .build();
    }
}
