package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Notificacion;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.NotificacionRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio de notificaciones a usuarios.
 */
@Service
public class NotificacionServiceImpl implements NotificacionService {

    // Repositorio de notificaciones.
    @Autowired
    private NotificacionRepository notificacionRepository;

    // Repositorio de usuarios (destinatario).
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Notificacion enviarNotificacion(Long usuarioId, Notificacion notificacion) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Se asigna destinatario, fecha del servidor y estado inicial "no leída".
        notificacion.setUsuario(usuario);
        notificacion.setFecha(LocalDateTime.now());
        notificacion.setLeida(false);

        return notificacionRepository.save(notificacion);
    }

    @Override
    public Optional<Notificacion> obtenerNotificacionPorId(Long id) {
        return notificacionRepository.findById(id);
    }

    @Override
    public List<Notificacion> listarNotificacionesPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public List<Notificacion> listarNotificacionesNoLeidas(Long usuarioId) {
        return notificacionRepository.findByLeidaFalseAndUsuarioId(usuarioId);
    }

    @Override
    public void marcarComoLeida(Long id) {
        // Si la notificación existe, se marca como leída; si no, no se hace nada.
        notificacionRepository.findById(id).ifPresent(notificacion -> {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        });
    }

    @Override
    public void eliminarNotificacion(Long id) {
        notificacionRepository.deleteById(id);
    }
}
