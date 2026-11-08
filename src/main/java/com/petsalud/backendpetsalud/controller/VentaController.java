package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Endpoints REST de ventas (/api/ventas).
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    // Servicio con la lógica de negocio.
    @Autowired
    private VentaService ventaService;

    /**
     * POST /api/ventas?usuarioId=...
     * El cuerpo es la lista de ids de productos a comprar.
     * Seguridad: VETERINARIO/ADMIN, o el propio cliente comprando para sí mismo.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @authz.esUsuario(#usuarioId)")
    @PostMapping
    public ResponseEntity<Venta> realizarVenta(
            @RequestParam Long usuarioId,
            @RequestBody List<Long> productosIds) {
        Venta nuevaVenta = ventaService.realizarVenta(usuarioId, productosIds);
        return ResponseEntity.ok(nuevaVenta);
    }

    /**
     * GET /api/ventas/{id}
     * Seguridad: VETERINARIO/ADMIN.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Long id) {
        return ventaService.obtenerVentaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/ventas/usuario/{usuarioId}
     * Seguridad: VETERINARIO/ADMIN, o el propio cliente.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @authz.esUsuario(#usuarioId)")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Venta>> listarVentasPorUsuario(@PathVariable Long usuarioId) {
        List<Venta> ventas = ventaService.listarVentasPorUsuario(usuarioId);
        return ResponseEntity.ok(ventas);
    }

    /**
     * GET /api/ventas
     * Seguridad: solo ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Venta>> listarTodasLasVentas() {
        List<Venta> ventas = ventaService.listarTodasLasVentas();
        return ResponseEntity.ok(ventas);
    }
}
