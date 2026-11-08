package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Recompensa;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.RecompensaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.RecompensaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio del sistema de fidelización (puntos por compras y servicios).
 */
@Service
public class RecompensaServiceImpl implements RecompensaService {

    // Repositorio de recompensas.
    @Autowired
    private RecompensaRepository recompensaRepository;

    // Repositorio de usuarios (beneficiario).
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Recompensa agregarRecompensa(Long usuarioId, Recompensa recompensa) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Se vincula la recompensa al usuario y se registra la fecha del servidor.
        recompensa.setUsuario(usuario);
        recompensa.setFecha(LocalDateTime.now());

        return recompensaRepository.save(recompensa);
    }

    @Override
    public Optional<Recompensa> obtenerRecompensaPorId(Long id) {
        return recompensaRepository.findById(id);
    }

    @Override
    public List<Recompensa> listarRecompensasPorUsuario(Long usuarioId) {
        return recompensaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public void eliminarRecompensa(Long id) {
        recompensaRepository.deleteById(id);
    }
}
