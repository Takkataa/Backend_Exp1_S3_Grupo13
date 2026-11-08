package com.petsalud.backendpetsalud.repository;

import com.petsalud.backendpetsalud.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de productos.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    /**
     * Busca un producto por nombre (para evitar duplicados al agregarlo).
     */
    Optional<Producto> findByNombre(String nombre);
    /**
     * Productos de una categoría.
     */
    List<Producto> findByCategoria(String categoria);
    /**
     * Productos con stock menor a la cantidad indicada (alerta de reposición).
     */
    List<Producto> findByStockLessThan(Integer cantidad);
}
