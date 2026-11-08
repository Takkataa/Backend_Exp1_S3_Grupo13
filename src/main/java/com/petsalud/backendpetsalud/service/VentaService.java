package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Venta;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de la lógica de negocio de ventas.
 */
public interface VentaService {
    /**
     * Registra una venta para un usuario, descuenta el stock y calcula el total.
     */
    Venta realizarVenta(Long usuarioId, List<Long> productosIds);
    /**
     * Busca una venta por id.
     */
    Optional<Venta> obtenerVentaPorId(Long id);
    /**
     * Compras de un usuario.
     */
    List<Venta> listarVentasPorUsuario(Long usuarioId);
    /**
     * Todas las ventas (uso administrativo).
     */
    List<Venta> listarTodasLasVentas();
}
