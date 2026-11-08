package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Cita;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de la lógica de negocio de citas veterinarias.
 */
public interface CitaService {
    /**
     * Agenda una cita para una mascota con un veterinario. Estado inicial: "Agendada".
     */
    Cita agendarCita(Long mascotaId, Long veterinarioId, LocalDateTime fechaHora, String motivo);
    /**
     * Busca una cita por id.
     */
    Optional<Cita> obtenerCitaPorId(Long id);
    /**
     * Cambia el estado de una cita (Agendada, Cancelada, Completada).
     */
    Cita actualizarEstadoCita(Long id, String nuevoEstado);
    /**
     * Cancela (elimina) una cita.
     */
    void cancelarCita(Long id);
    /**
     * Citas de una mascota.
     */
    List<Cita> listarCitasPorMascota(Long mascotaId);
    /**
     * Citas de un veterinario.
     */
    List<Cita> listarCitasPorVeterinario(Long veterinarioId);
    /**
     * Citas dentro de un rango de fechas.
     */
    List<Cita> listarCitasEntreFechas(LocalDateTime inicio, LocalDateTime fin);
}
