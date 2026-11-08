package com.petsalud.backendpetsalud.repository;

import com.petsalud.backendpetsalud.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a datos de mascotas.
 */
@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    /**
     * Mascotas de un propietario (usuario) dado.
     */
    List<Mascota> findByPropietarioId(Long propietarioId);
}
