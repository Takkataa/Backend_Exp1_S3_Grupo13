package com.petsalud.backendpetsalud.repository;

import com.petsalud.backendpetsalud.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de citas.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    /**
     * Citas de una mascota.
     */
    List<Cita> findByMascotaId(Long mascotaId);
    /**
     * Citas asignadas a un veterinario.
     */
    List<Cita> findByVeterinarioId(Long veterinarioId);
    /**
     * Citas cuya fecha y hora está dentro del rango [inicio, fin].
     */
    List<Cita> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
}
