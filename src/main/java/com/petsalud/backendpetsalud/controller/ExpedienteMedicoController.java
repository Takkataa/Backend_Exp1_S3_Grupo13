package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.ExpedienteMedico;
import com.petsalud.backendpetsalud.service.ExpedienteMedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Endpoints REST del historial médico (/api/expedientes).
 * Son datos clínicos sensibles (Ley 19.628 sobre protección de datos personales), por eso el acceso es restringido.
 */
@RestController
@RequestMapping("/api/expedientes")
public class ExpedienteMedicoController {

    // Servicio con la lógica de negocio.
    @Autowired
    private ExpedienteMedicoService expedienteMedicoService;

    /**
     * POST /api/expedientes
     * Seguridad: VETERINARIO/ADMIN.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    @PostMapping
    public ResponseEntity<ExpedienteMedico> agregarRegistro(
            @RequestParam Long mascotaId,
            @RequestParam Long veterinarioId,
            @RequestBody ExpedienteMedico registro) {
        ExpedienteMedico nuevoRegistro = expedienteMedicoService.agregarRegistro(mascotaId, veterinarioId, registro);
        return ResponseEntity.ok(nuevoRegistro);
    }

    /**
     * GET /api/expedientes/{id}
     * Seguridad: VETERINARIO/ADMIN.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ExpedienteMedico> obtenerRegistroPorId(@PathVariable Long id) {
        return expedienteMedicoService.obtenerRegistroPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/expedientes/mascota/{mascotaId}
     * Seguridad: VETERINARIO/ADMIN, o el dueño de la mascota.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @authz.esDuenoMascota(#mascotaId)")
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<ExpedienteMedico>> listarRegistrosPorMascota(@PathVariable Long mascotaId) {
        List<ExpedienteMedico> registros = expedienteMedicoService.listarRegistrosPorMascota(mascotaId);
        return ResponseEntity.ok(registros);
    }

    /**
     * DELETE /api/expedientes/{id}
     * Seguridad: solo ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRegistro(@PathVariable Long id) {
        expedienteMedicoService.eliminarRegistro(id);
        return ResponseEntity.noContent().build();
    }
}
