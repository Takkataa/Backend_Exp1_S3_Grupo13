package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.ExpedienteMedico;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de la lógica de negocio del historial médico.
 */
public interface ExpedienteMedicoService {
    /**
     * Agrega un registro médico a una mascota, indicando el veterinario que lo realiza.
     */
    ExpedienteMedico agregarRegistro(Long mascotaId, Long veterinarioId, ExpedienteMedico registro);
    /**
     * Busca un registro por id.
     */
    Optional<ExpedienteMedico> obtenerRegistroPorId(Long id);
    /**
     * Historial completo de una mascota.
     */
    List<ExpedienteMedico> listarRegistrosPorMascota(Long mascotaId);
    /**
     * Elimina un registro.
     */
    void eliminarRegistro(Long id);
}
