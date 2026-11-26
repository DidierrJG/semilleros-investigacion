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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramaAcademicoService {

    private final ProgramaAcademicoRepository programaRepository;
    private final IntegranteRepository integranteRepository;
    private final FacultadRepository facultadRepository;

    @Transactional(readOnly = true)
    public List<ProgramaAcademicoResponseDTO> findAll() {
        return programaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgramaAcademicoResponseDTO findById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional
    public ProgramaAcademicoResponseDTO create(ProgramaAcademicoRequestDTO request) {
        String nombre = request.getNombre().trim();
        if (programaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BusinessException("Ya existe un programa con el nombre: " + nombre);
        }

        Facultad facultad = null;
        if (request.getFacultadId() != null) {
            facultad = facultadRepository.findById(request.getFacultadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Facultad no encontrada con ID: " + request.getFacultadId()));
        }

        ProgramaAcademico programa = ProgramaAcademico.builder()
                .nombre(nombre)
                .facultad(facultad)
                .build();
        return toResponseDTO(programaRepository.save(programa));
    }

    @Transactional
    public ProgramaAcademicoResponseDTO update(Long id, ProgramaAcademicoRequestDTO request) {
        ProgramaAcademico programa = findEntityById(id);
        String nombre = request.getNombre().trim();
        if (programaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new BusinessException("Ya existe otro programa con el nombre: " + nombre);
        }

        Facultad facultad = null;
        if (request.getFacultadId() != null) {
            facultad = facultadRepository.findById(request.getFacultadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Facultad no encontrada con ID: " + request.getFacultadId()));
        }

        programa.setNombre(nombre);
        programa.setFacultad(facultad);
        return toResponseDTO(programaRepository.save(programa));
    }

    @Transactional
    public void delete(Long id) {
        ProgramaAcademico programa = findEntityById(id);
        if (integranteRepository.existsByProgramaId(id)) {
            throw new BusinessException("No se puede eliminar el programa porque tiene integrantes asociados");
        }
        programaRepository.delete(programa);
    }

    public ProgramaAcademico findEntityById(Long id) {
        return programaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa académico no encontrado con id: " + id));
    }

    public ProgramaAcademicoResponseDTO toResponseDTO(ProgramaAcademico programa) {
        ProgramaAcademicoResponseDTO.FacultadSummaryDTO facultadDTO = null;
        if (programa.getFacultad() != null) {
            Facultad f = programa.getFacultad();
            facultadDTO = ProgramaAcademicoResponseDTO.FacultadSummaryDTO.builder()
                    .id(f.getId())
                    .nombre(f.getNombre())
                    .sigla(f.getSigla())
                    .build();
        }

        return ProgramaAcademicoResponseDTO.builder()
                .id(programa.getId())
                .nombre(programa.getNombre())
                .facultad(facultadDTO)
                .build();
    }
}
