package com.petsalud.backendpetsalud.repository;

import com.petsalud.backendpetsalud.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de ventas.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    /**
     * Compras de un cliente.
     */
    List<Venta> findByUsuarioId(Long usuarioId);
    /**
     * Ventas dentro de un rango de fechas (base para reportes).
     */
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}
