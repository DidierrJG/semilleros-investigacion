package com.semilleros.backend.service;

import com.semilleros.backend.dto.PersonaRequestDTO;
import com.semilleros.backend.dto.PersonaResponseDTO;
import com.semilleros.backend.dto.RolResponseDTO;
import com.semilleros.backend.entity.Departamento;
import com.semilleros.backend.entity.Persona;
import com.semilleros.backend.entity.Rol;
import com.semilleros.backend.exception.BusinessException;
import com.semilleros.backend.exception.ResourceNotFoundException;
import com.semilleros.backend.repository.DepartamentoRepository;
import com.semilleros.backend.repository.PersonaRepository;
import com.semilleros.backend.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final RolRepository rolRepository;

    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> findAll() {
        return personaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PersonaResponseDTO findById(Long id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Persona findEntityById(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> findByDepartamentoId(Long departamentoId) {
        return personaRepository.findByDepartamentoId(departamentoId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PersonaResponseDTO> findByRol(String rolCodigo) {
        return personaRepository.findByRolCodigo(rolCodigo)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RolResponseDTO> findAllRoles() {
        return rolRepository.findAll()
                .stream()
                .map(r -> RolResponseDTO.builder()
                        .id(r.getId())
                        .codigo(r.getCodigo())
                        .nombre(r.getNombre())
                        .descripcion(r.getDescripcion())
                        .build())
                .toList();
    }

    @Transactional
    public PersonaResponseDTO create(PersonaRequestDTO request) {
        String identificacion = request.getIdentificacion().trim();
        String correo = request.getCorreo().trim().toLowerCase();

        if (personaRepository.existsByIdentificacion(identificacion)) {
            throw new BusinessException("Ya existe una persona con la identificación: " + identificacion);
        }
        if (personaRepository.existsByCorreoIgnoreCase(correo)) {
            throw new BusinessException("Ya existe una persona con el correo: " + correo);
        }

        Departamento departamento = null;
        if (request.getDepartamentoId() != null) {
            departamento = departamentoRepository.findById(request.getDepartamentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado con ID: " + request.getDepartamentoId()));
        }

        Set<Rol> roles = resolveRoles(request.getRolCodigos());

        Persona persona = Persona.builder()
                .identificacion(identificacion)
                .tipoDocumento(request.getTipoDocumento().trim().toUpperCase())
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .correo(correo)
                .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                .cvlacUrl(request.getCvlacUrl() != null ? request.getCvlacUrl().trim() : null)
                .departamento(departamento)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .roles(roles)
                .build();

        return toResponseDTO(personaRepository.save(persona));
    }

    @Transactional
    public PersonaResponseDTO update(Long id, PersonaRequestDTO request) {
        Persona persona = findEntityById(id);
        String identificacion = request.getIdentificacion().trim();
        String correo = request.getCorreo().trim().toLowerCase();

        if (personaRepository.existsByIdentificacionAndIdNot(identificacion, id)) {
            throw new BusinessException("Ya existe otra persona con la identificación: " + identificacion);
        }
        if (personaRepository.existsByCorreoIgnoreCaseAndIdNot(correo, id)) {
            throw new BusinessException("Ya existe otra persona con el correo: " + correo);
        }

        Departamento departamento = null;
        if (request.getDepartamentoId() != null) {
            departamento = departamentoRepository.findById(request.getDepartamentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado con ID: " + request.getDepartamentoId()));
        }

        Set<Rol> roles = resolveRoles(request.getRolCodigos());

        persona.setIdentificacion(identificacion);
        persona.setTipoDocumento(request.getTipoDocumento().trim().toUpperCase());
        persona.setNombres(request.getNombres().trim());
        persona.setApellidos(request.getApellidos().trim());
        persona.setCorreo(correo);
        persona.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        persona.setCvlacUrl(request.getCvlacUrl() != null ? request.getCvlacUrl().trim() : null);
        persona.setDepartamento(departamento);
        if (request.getActivo() != null) {
            persona.setActivo(request.getActivo());
        }
        persona.setRoles(roles);

        return toResponseDTO(personaRepository.save(persona));
    }

    @Transactional
    public void delete(Long id) {
        Persona persona = findEntityById(id);
        personaRepository.delete(persona);
    }

    private Set<Rol> resolveRoles(Set<String> rolCodigos) {
        Set<Rol> roles = new HashSet<>();
        if (rolCodigos != null && !rolCodigos.isEmpty()) {
            for (String codigo : rolCodigos) {
                Rol rol = rolRepository.findByCodigo(codigo.trim().toUpperCase())
                        .orElseThrow(() -> new BusinessException("Rol no válido: " + codigo));
                roles.add(rol);
            }
        }
        return roles;
    }

    public PersonaResponseDTO toResponseDTO(Persona persona) {
        PersonaResponseDTO.DepartamentoSummaryDTO deptoDTO = null;
        if (persona.getDepartamento() != null) {
            Departamento d = persona.getDepartamento();
            deptoDTO = PersonaResponseDTO.DepartamentoSummaryDTO.builder()
                    .id(d.getId())
                    .nombre(d.getNombre())
                    .facultadId(d.getFacultad() != null ? d.getFacultad().getId() : null)
                    .facultadNombre(d.getFacultad() != null ? d.getFacultad().getNombre() : null)
                    .build();
        }

        List<RolResponseDTO> rolesDTO = (persona.getRoles() != null)
                ? persona.getRoles().stream()
                .map(r -> RolResponseDTO.builder()
                        .id(r.getId())
                        .codigo(r.getCodigo())
                        .nombre(r.getNombre())
                        .descripcion(r.getDescripcion())
                        .build())
                .toList()
                : List.of();

        return PersonaResponseDTO.builder()
                .id(persona.getId())
                .identificacion(persona.getIdentificacion())
                .tipoDocumento(persona.getTipoDocumento())
                .nombres(persona.getNombres())
                .apellidos(persona.getApellidos())
                .nombreCompleto(persona.getNombreCompleto())
                .correo(persona.getCorreo())
                .telefono(persona.getTelefono())
                .cvlacUrl(persona.getCvlacUrl())
                .activo(persona.getActivo())
                .departamento(deptoDTO)
                .roles(rolesDTO)
                .build();
    }
}
