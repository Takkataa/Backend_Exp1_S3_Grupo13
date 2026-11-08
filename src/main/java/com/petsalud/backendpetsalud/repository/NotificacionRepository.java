package com.petsalud.backendpetsalud.repository;

import com.petsalud.backendpetsalud.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de notificaciones.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    /**
     * Todas las notificaciones de un usuario.
     */
    List<Notificacion> findByUsuarioId(Long usuarioId);
    /**
     * Solo las notificaciones no leídas de un usuario.
     */
    List<Notificacion> findByLeidaFalseAndUsuarioId(Long usuarioId);
}
