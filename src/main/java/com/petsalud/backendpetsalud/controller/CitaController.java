package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Cita;
import com.petsalud.backendpetsalud.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Endpoints REST de citas veterinarias (/api/citas).
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    // Servicio con la lógica de negocio.
    @Autowired
    private CitaService citaService;

    /**
     * POST /api/citas
     * Agenda una cita. La fecha llega en formato ISO 8601 (ej: 2026-12-01T10:30:00).
     * Seguridad: VETERINARIO/ADMIN, o un CLIENTE que sea dueño de la mascota.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or (hasRole('CLIENTE') and @authz.esDuenoMascota(#mascotaId))")
    @PostMapping
    public ResponseEntity<Cita> agendarCita(
            @RequestParam Long mascotaId,
            @RequestParam Long veterinarioId,
            @RequestParam String fechaHora, // Formato ISO 8601
            @RequestParam String motivo) {
        LocalDateTime fechaHoraParsed = LocalDateTime.parse(fechaHora);
        Cita nuevaCita = citaService.agendarCita(mascotaId, veterinarioId, fechaHoraParsed, motivo);
        return ResponseEntity.ok(nuevaCita);
    }

    /**
     * GET /api/citas/{id}
     * Seguridad: VETERINARIO/ADMIN.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/citas/{id}/estado
     * Seguridad: VETERINARIO/ADMIN.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<Cita> actualizarEstadoCita(@PathVariable Long id, @RequestParam String estado) {
        Cita citaActualizada = citaService.actualizarEstadoCita(id, estado);
        return ResponseEntity.ok(citaActualizada);
    }

    /**
     * DELETE /api/citas/{id}
     * Seguridad: VETERINARIO/ADMIN.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/citas/mascota/{mascotaId}
     * Seguridad: VETERINARIO/ADMIN, o el dueño de la mascota.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN') or @authz.esDuenoMascota(#mascotaId)")
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<Cita>> listarCitasPorMascota(@PathVariable Long mascotaId) {
        List<Cita> citas = citaService.listarCitasPorMascota(mascotaId);
        return ResponseEntity.ok(citas);
    }

    /**
     * GET /api/citas/veterinario/{veterinarioId}
     * Seguridad: VETERINARIO/ADMIN.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<List<Cita>> listarCitasPorVeterinario(@PathVariable Long veterinarioId) {
        List<Cita> citas = citaService.listarCitasPorVeterinario(veterinarioId);
        return ResponseEntity.ok(citas);
    }

    /**
     * GET /api/citas/entre-fechas?inicio=...&fin=...
     * Seguridad: VETERINARIO/ADMIN.
     */
    @PreAuthorize("hasAnyRole('VETERINARIO','ADMIN')")
    @GetMapping("/entre-fechas")
    public ResponseEntity<List<Cita>> listarCitasEntreFechas(
            @RequestParam String inicio,
            @RequestParam String fin) {
        LocalDateTime inicioParsed = LocalDateTime.parse(inicio);
        LocalDateTime finParsed = LocalDateTime.parse(fin);
        List<Cita> citas = citaService.listarCitasEntreFechas(inicioParsed, finParsed);
        return ResponseEntity.ok(citas);
    }
}
