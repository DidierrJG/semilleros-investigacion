package com.semilleros.backend.service;

import com.semilleros.backend.dto.DepartamentoRequestDTO;
import com.semilleros.backend.dto.DepartamentoResponseDTO;
import com.semilleros.backend.dto.PersonaSummaryDTO;
import com.semilleros.backend.entity.Departamento;
import com.semilleros.backend.entity.Facultad;
import com.semilleros.backend.entity.Persona;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.DepartamentoRepository;
import com.semilleros.backend.repository.FacultadRepository;
import com.semilleros.backend.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final FacultadRepository facultadRepository;
    private final PersonaRepository personaRepository;

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> findAll() {
        return departamentoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> findByFacultadId(Long facultadId) {
        return departamentoRepository.findByFacultadId(facultadId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartamentoResponseDTO findById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Departamento findEntityById(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado con ID: " + id));
    }

    @Transactional
    public DepartamentoResponseDTO create(DepartamentoRequestDTO request) {
        String nombre = request.getNombre().trim();
        Facultad facultad = facultadRepository.findById(request.getFacultadId())
                .orElseThrow(() -> new ResourceNotFoundException("Facultad no encontrada con ID: " + request.getFacultadId()));

        if (departamentoRepository.existsByNombreIgnoreCaseAndFacultadId(nombre, facultad.getId())) {
            throw new BusinessException("Ya existe un departamento con el nombre '" + nombre + "' en la facultad: " + facultad.getNombre());
        }

        Persona director = null;
        if (request.getDirectorId() != null) {
            director = personaRepository.findById(request.getDirectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada para director con ID: " + request.getDirectorId()));
        }

        Departamento departamento = Departamento.builder()
                .nombre(nombre)
                .facultad(facultad)
                .director(director)
                .build();

        return toResponseDTO(departamentoRepository.save(departamento));
    }

    @Transactional
    public DepartamentoResponseDTO update(Long id, DepartamentoRequestDTO request) {
        Departamento departamento = findEntityById(id);
        String nombre = request.getNombre().trim();
        Facultad facultad = facultadRepository.findById(request.getFacultadId())
                .orElseThrow(() -> new ResourceNotFoundException("Facultad no encontrada con ID: " + request.getFacultadId()));

        if (departamentoRepository.existsByNombreIgnoreCaseAndFacultadIdAndIdNot(nombre, facultad.getId(), id)) {
            throw new BusinessException("Ya existe otro departamento con el nombre '" + nombre + "' en la facultad: " + facultad.getNombre());
        }

        Persona director = null;
        if (request.getDirectorId() != null) {
            director = personaRepository.findById(request.getDirectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada para director con ID: " + request.getDirectorId()));
        }

        departamento.setNombre(nombre);
        departamento.setFacultad(facultad);
        departamento.setDirector(director);

        return toResponseDTO(departamentoRepository.save(departamento));
    }

    @Transactional
    public void delete(Long id) {
        Departamento departamento = findEntityById(id);
        departamentoRepository.delete(departamento);
    }

    public DepartamentoResponseDTO toResponseDTO(Departamento departamento) {
        PersonaSummaryDTO directorDTO = null;
        if (departamento.getDirector() != null) {
            Persona d = departamento.getDirector();
            directorDTO = PersonaSummaryDTO.builder()
                    .id(d.getId())
                    .identificacion(d.getIdentificacion())
                    .nombreCompleto(d.getNombreCompleto())
                    .correo(d.getCorreo())
                    .build();
        }

        DepartamentoResponseDTO.FacultadSummaryDTO facultadDTO = null;
        if (departamento.getFacultad() != null) {
            Facultad f = departamento.getFacultad();
            facultadDTO = DepartamentoResponseDTO.FacultadSummaryDTO.builder()
                    .id(f.getId())
                    .nombre(f.getNombre())
                    .sigla(f.getSigla())
                    .build();
        }

        return DepartamentoResponseDTO.builder()
                .id(departamento.getId())
                .nombre(departamento.getNombre())
                .facultad(facultadDTO)
                .director(directorDTO)
                .build();
    }
}
