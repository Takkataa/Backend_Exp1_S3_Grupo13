package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Recompensa;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de la lógica de negocio del programa de fidelización.
 */
public interface RecompensaService {
    /**
     * Otorga una recompensa (puntos) a un usuario.
     */
    Recompensa agregarRecompensa(Long usuarioId, Recompensa recompensa);
    /**
     * Busca una recompensa por id.
     */
    Optional<Recompensa> obtenerRecompensaPorId(Long id);
    /**
     * Recompensas de un usuario.
     */
    List<Recompensa> listarRecompensasPorUsuario(Long usuarioId);
    /**
     * Elimina una recompensa.
     */
    void eliminarRecompensa(Long id);
}
