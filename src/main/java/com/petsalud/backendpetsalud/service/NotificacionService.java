package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Notificacion;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de la lógica de negocio de notificaciones.
 */
public interface NotificacionService {
    /**
     * Crea una notificación para un usuario (queda como no leída).
     */
    Notificacion enviarNotificacion(Long usuarioId, Notificacion notificacion);
    /**
     * Busca una notificación por id.
     */
    Optional<Notificacion> obtenerNotificacionPorId(Long id);
    /**
     * Todas las notificaciones de un usuario.
     */
    List<Notificacion> listarNotificacionesPorUsuario(Long usuarioId);
    /**
     * Notificaciones no leídas de un usuario.
     */
    List<Notificacion> listarNotificacionesNoLeidas(Long usuarioId);
    /**
     * Marca una notificación como leída.
     */
    void marcarComoLeida(Long id);
    /**
     * Elimina una notificación.
     */
    void eliminarNotificacion(Long id);
}
