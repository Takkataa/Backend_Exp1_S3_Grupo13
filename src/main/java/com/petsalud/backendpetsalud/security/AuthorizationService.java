package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.NotificacionRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Comprobaciones de propiedad usadas en expresiones @PreAuthorize
 * (por ejemplo: un cliente solo puede ver sus propios datos y mascotas).
 */
@Component("authz")
public class AuthorizationService {

    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;
    private final NotificacionRepository notificacionRepository;

    public AuthorizationService(UsuarioRepository usuarioRepository,
                                MascotaRepository mascotaRepository,
                                NotificacionRepository notificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.notificacionRepository = notificacionRepository;
    }

    private String emailActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null) ? null : auth.getName();
    }

    /** El usuario autenticado es el usuario con ese id. */
    public boolean esUsuario(Long usuarioId) {
        String email = emailActual();
        if (email == null || usuarioId == null) return false;
        return usuarioRepository.findByEmail(email)
                .map(u -> u.getId().equals(usuarioId))
                .orElse(false);
    }

    /** El usuario autenticado es el propietario de la mascota. */
    public boolean esDuenoMascota(Long mascotaId) {
        String email = emailActual();
        if (email == null || mascotaId == null) return false;
        return mascotaRepository.findById(mascotaId)
                .map(m -> m.getPropietario() != null && email.equals(m.getPropietario().getEmail()))
                .orElse(false);
    }

    /** La notificacion pertenece al usuario autenticado. */
    public boolean esDuenoNotificacion(Long notificacionId) {
        String email = emailActual();
        if (email == null || notificacionId == null) return false;
        return notificacionRepository.findById(notificacionId)
                .map(n -> n.getUsuario() != null && email.equals(n.getUsuario().getEmail()))
                .orElse(false);
    }
}
