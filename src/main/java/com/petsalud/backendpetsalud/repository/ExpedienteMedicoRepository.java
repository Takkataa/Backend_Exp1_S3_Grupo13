package com.petsalud.backendpetsalud.repository;

import com.petsalud.backendpetsalud.entity.ExpedienteMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos del historial médico.
 */
@Repository
public interface ExpedienteMedicoRepository extends JpaRepository<ExpedienteMedico, Long> {
    /**
     * Historial médico completo de una mascota.
     */
    List<ExpedienteMedico> findByMascotaId(Long mascotaId);
}
