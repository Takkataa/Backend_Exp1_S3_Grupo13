package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Endpoints REST de mascotas (/api/mascotas).
 */
@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    // Servicio con la lógica de negocio.
    @Autowired
    private MascotaService mascotaService;

    /**
     * POST /api/mascotas/propietario/{propietarioId}
     * Seguridad: VETERINARIO/ADMIN, o el propio cliente registrando sus mascotas.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @authz.esUsuario(#propietarioId)")
    @PostMapping("/propietario/{propietarioId}")
    public ResponseEntity<Mascota> registrarMascota(@PathVariable Long propietarioId, @RequestBody Mascota mascota) {
        Mascota nuevaMascota = mascotaService.registrarMascota(mascota, propietarioId);
        return ResponseEntity.ok(nuevaMascota);
    }

    /**
     * GET /api/mascotas/{id}
     * Seguridad: VETERINARIO/ADMIN, o el dueño de la mascota.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @authz.esDuenoMascota(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<Mascota> obtenerMascotaPorId(@PathVariable Long id) {
        return mascotaService.obtenerMascotaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/mascotas/{id}
     * Seguridad: VETERINARIO/ADMIN, o el dueño de la mascota.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @authz.esDuenoMascota(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizarMascota(@PathVariable Long id, @RequestBody Mascota mascota) {
        Mascota mascotaActualizada = mascotaService.actualizarMascota(id, mascota);
        return ResponseEntity.ok(mascotaActualizada);
    }

    /**
     * DELETE /api/mascotas/{id}
     * Seguridad: solo ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable Long id) {
        mascotaService.eliminarMascota(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/mascotas/propietario/{propietarioId}
     * Seguridad: VETERINARIO/ADMIN, o el propio propietario.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @authz.esUsuario(#propietarioId)")
    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<List<Mascota>> listarMascotasPorPropietario(@PathVariable Long propietarioId) {
        List<Mascota> mascotas = mascotaService.listarMascotasPorPropietario(propietarioId);
        return ResponseEntity.ok(mascotas);
    }
}
