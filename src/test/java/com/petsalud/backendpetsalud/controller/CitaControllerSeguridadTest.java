package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Cita;
import com.petsalud.backendpetsalud.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import com.petsalud.backendpetsalud.support.SeguridadTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorización en /api/citas.
 * Regla: VETERINARIO/ADMIN gestionan todas las citas; un CLIENTE solo agenda y lista citas de SUS mascotas.
 */
class CitaControllerSeguridadTest extends SeguridadTestBase {

    @Autowired
    private CitaRepository citaRepository;

    private Cita cita;

    @BeforeEach
    void crearCita() {
        cita = citaRepository.save(Cita.builder()
                .fechaHora(LocalDateTime.of(2026, 12, 1, 10, 30))
                .estado("Agendada").motivo("Control anual")
                .mascota(mascotaCliente).veterinario(veterinario)
                .build());
    }

    private String urlAgendar() {
        return "/api/citas?mascotaId=" + mascotaCliente.getId() + "&veterinarioId=" + veterinario.getId()
                + "&fechaHora=2026-12-02T11:00:00&motivo=Vacuna";
    }

    @Test
    @DisplayName("POST /api/citas: sin token 401")
    void agendar_sinToken_401() throws Exception {
        post(urlAgendar(), null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/citas: el dueño agenda para su mascota")
    void agendar_dueno_permitido() throws Exception {
        post(urlAgendar(), tokenCliente).andExpect(permitido());
    }

    @Test
    @DisplayName("POST /api/citas: otro cliente NO puede agendar para una mascota ajena (403)")
    void agendar_otroCliente_403() throws Exception {
        post(urlAgendar(), tokenOtroCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/citas: VETERINARIO puede agendar")
    void agendar_veterinario_permitido() throws Exception {
        post(urlAgendar(), tokenVeterinario).andExpect(permitido());
    }

    @Test
    @DisplayName("GET /api/citas/{id}: CLIENTE 403, incluso siendo el dueño")
    void verPorId_cliente_403() throws Exception {
        get("/api/citas/" + cita.getId(), tokenCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/citas/{id}: VETERINARIO permitido")
    void verPorId_veterinario_permitido() throws Exception {
        get("/api/citas/" + cita.getId(), tokenVeterinario).andExpect(permitido());
    }

    @Test
    @DisplayName("PUT /api/citas/{id}/estado: CLIENTE 403")
    void cambiarEstado_cliente_403() throws Exception {
        put("/api/citas/" + cita.getId() + "/estado?estado=Completada", tokenCliente)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/citas/{id}/estado: VETERINARIO permitido")
    void cambiarEstado_veterinario_permitido() throws Exception {
        put("/api/citas/" + cita.getId() + "/estado?estado=Completada", tokenVeterinario)
                .andExpect(permitido());
    }

    @Test
    @DisplayName("DELETE /api/citas/{id}: CLIENTE 403 (el dueño no puede cancelar su propia cita)")
    void cancelar_cliente_403() throws Exception {
        delete("/api/citas/" + cita.getId(), tokenCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/citas/{id}: VETERINARIO 204")
    void cancelar_veterinario_204() throws Exception {
        delete("/api/citas/" + cita.getId(), tokenVeterinario).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/citas/mascota/{id}: el dueño lista las citas de su mascota")
    void listarPorMascota_dueno_permitido() throws Exception {
        get("/api/citas/mascota/" + mascotaCliente.getId(), tokenCliente).andExpect(permitido());
    }

    @Test
    @DisplayName("GET /api/citas/mascota/{id}: otro cliente 403")
    void listarPorMascota_otroCliente_403() throws Exception {
        get("/api/citas/mascota/" + mascotaCliente.getId(), tokenOtroCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/citas/veterinario/{id}: CLIENTE 403")
    void listarPorVeterinario_cliente_403() throws Exception {
        get("/api/citas/veterinario/" + veterinario.getId(), tokenCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/citas/entre-fechas: CLIENTE 403 y VETERINARIO permitido")
    void listarEntreFechas_porRol() throws Exception {
        String url = "/api/citas/entre-fechas?inicio=2026-12-01T00:00:00&fin=2026-12-31T23:59:59";
        get(url, tokenCliente).andExpect(status().isForbidden());
        get(url, tokenVeterinario).andExpect(permitido());
    }
}
