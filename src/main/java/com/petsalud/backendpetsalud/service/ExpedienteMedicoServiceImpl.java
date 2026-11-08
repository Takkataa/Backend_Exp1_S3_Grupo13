package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.ExpedienteMedico;
import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.ExpedienteMedicoRepository;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.ExpedienteMedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio del historial médico de las mascotas.
 */
@Service
public class ExpedienteMedicoServiceImpl implements ExpedienteMedicoService {

    // Repositorio de expedientes.
    @Autowired
    private ExpedienteMedicoRepository expedienteMedicoRepository;

    // Repositorio de mascotas.
    @Autowired
    private MascotaRepository mascotaRepository;

    // Repositorio de usuarios (veterinario que firma el registro).
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public ExpedienteMedico agregarRegistro(Long mascotaId, Long veterinarioId, ExpedienteMedico registro) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));

        Usuario veterinario = usuarioRepository.findById(veterinarioId)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario no encontrado"));

        // Se vinculan la mascota y el veterinario, y la fecha la fija el servidor (el cliente no puede falsearla).
        registro.setMascota(mascota);
        registro.setVeterinario(veterinario);
        registro.setFecha(LocalDateTime.now());

        return expedienteMedicoRepository.save(registro);
    }

    @Override
    public Optional<ExpedienteMedico> obtenerRegistroPorId(Long id) {
        return expedienteMedicoRepository.findById(id);
    }

    @Override
    public List<ExpedienteMedico> listarRegistrosPorMascota(Long mascotaId) {
        return expedienteMedicoRepository.findByMascotaId(mascotaId);
    }

    @Override
    public void eliminarRegistro(Long id) {
        expedienteMedicoRepository.deleteById(id);
    }
}
