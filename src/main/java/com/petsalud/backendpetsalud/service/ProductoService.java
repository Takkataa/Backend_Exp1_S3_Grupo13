package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de la lógica de negocio del inventario de productos.
 */
public interface ProductoService {
    /**
     * Agrega un producto nuevo (no permite nombres duplicados).
     */
    Producto agregarProducto(Producto producto);
    /**
     * Busca un producto por id.
     */
    Optional<Producto> obtenerProductoPorId(Long id);
    /**
     * Actualiza los datos de un producto.
     */
    Producto actualizarProducto(Long id, Producto producto);
    /**
     * Elimina un producto.
     */
    void eliminarProducto(Long id);
    /**
     * Lista todos los productos.
     */
    List<Producto> listarProductos();
    /**
     * Lista los productos de una categoría.
     */
    List<Producto> listarProductosPorCategoria(String categoria);
    /**
     * Lista los productos con stock menor a la cantidad indicada.
     */
    List<Producto> listarProductosConStockBajo(Integer cantidad);
}
