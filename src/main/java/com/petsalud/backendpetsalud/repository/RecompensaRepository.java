package com.petsalud.backendpetsalud.repository;

import com.petsalud.backendpetsalud.entity.Recompensa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de recompensas.
 */
@Repository
public interface RecompensaRepository extends JpaRepository<Recompensa, Long> {
    /**
     * Recompensas acumuladas por un usuario.
     */
    List<Recompensa> findByUsuarioId(Long usuarioId);
}
