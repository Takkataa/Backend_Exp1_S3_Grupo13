package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.support.SeguridadTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorización en /api/expedientes (datos clínicos sensibles).
 * Regla: solo VETERINARIO/ADMIN crean y consultan registros; el dueño solo puede ver el historial de SU mascota;
 * eliminar es solo ADMIN.
 */
class ExpedienteMedicoControllerSeguridadTest extends SeguridadTestBase {

    private static final String REGISTRO_JSON = """
            {"descripcion": "Consulta general", "diagnostico": "Otitis", "tratamiento": "Gotas 7 días"}
            """;

    private String urlCrear() {
        return "/api/expedientes?mascotaId=" + mascotaCliente.getId() + "&veterinarioId=" + veterinario.getId();
    }

    @Test
    @DisplayName("POST /api/expedientes: sin token 401")
    void crear_sinToken_401() throws Exception {
        post(urlCrear(), null, REGISTRO_JSON).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/expedientes: CLIENTE 403, ni siquiera para su propia mascota")
    void crear_cliente_403() throws Exception {
        post(urlCrear(), tokenCliente, REGISTRO_JSON).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/expedientes: VETERINARIO permitido")
    void crear_veterinario_permitido() throws Exception {
        post(urlCrear(), tokenVeterinario, REGISTRO_JSON).andExpect(permitido());
    }

    @Test
    @DisplayName("GET /api/expedientes/{id}: CLIENTE 403")
    void verPorId_cliente_403() throws Exception {
        get("/api/expedientes/999999", tokenCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/expedientes/{id}: VETERINARIO pasa la seguridad (404 si no existe)")
    void verPorId_veterinario_404() throws Exception {
        get("/api/expedientes/999999", tokenVeterinario).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/expedientes/mascota/{id}: el dueño ve el historial de su mascota")
    void historial_dueno_200() throws Exception {
        get("/api/expedientes/mascota/" + mascotaCliente.getId(), tokenCliente).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/expedientes/mascota/{id}: otro cliente 403")
    void historial_otroCliente_403() throws Exception {
        get("/api/expedientes/mascota/" + mascotaCliente.getId(), tokenOtroCliente)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/expedientes/mascota/{id}: VETERINARIO 200")
    void historial_veterinario_200() throws Exception {
        get("/api/expedientes/mascota/" + mascotaCliente.getId(), tokenVeterinario).andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/expedientes/{id}: VETERINARIO 403 y ADMIN 204")
    void eliminar_porRol() throws Exception {
        delete("/api/expedientes/999999", tokenVeterinario).andExpect(status().isForbidden());
        delete("/api/expedientes/999999", tokenAdmin).andExpect(status().isNoContent());
    }
}
