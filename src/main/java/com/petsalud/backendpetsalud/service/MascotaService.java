package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Mascota;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de la lógica de negocio de mascotas.
 */
public interface MascotaService {
    /**
     * Registra una mascota y la asocia al propietario indicado.
     */
    Mascota registrarMascota(Mascota mascota, Long propietarioId);
    /**
     * Busca una mascota por id.
     */
    Optional<Mascota> obtenerMascotaPorId(Long id);
    /**
     * Actualiza los datos de una mascota.
     */
    Mascota actualizarMascota(Long id, Mascota mascotaActualizada);
    /**
     * Elimina una mascota.
     */
    void eliminarMascota(Long id);
    /**
     * Lista las mascotas de un propietario.
     */
    List<Mascota> listarMascotasPorPropietario(Long propietarioId);
}
