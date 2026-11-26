package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.support.SeguridadTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorización en /api/mascotas.
 * Regla: VETERINARIO/ADMIN acceden a todas; un CLIENTE solo a sus propias mascotas; eliminar es solo ADMIN.
 */
class MascotaControllerSeguridadTest extends SeguridadTestBase {

    private static final String MASCOTA_JSON = """
            {"nombre": "Michi", "especie": "Gato", "raza": "Siamés", "sexo": "H"}
            """;

    @Test
    @DisplayName("GET /api/mascotas/{id}: sin token 401")
    void ver_sinToken_401() throws Exception {
        get("/api/mascotas/" + mascotaCliente.getId(), null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/mascotas/{id}: el dueño puede verla")
    void ver_dueno_permitido() throws Exception {
        get("/api/mascotas/" + mascotaCliente.getId(), tokenCliente).andExpect(permitido());
    }

    @Test
    @DisplayName("GET /api/mascotas/{id}: otro cliente NO puede verla (403)")
    void ver_otroCliente_403() throws Exception {
        get("/api/mascotas/" + mascotaCliente.getId(), tokenOtroCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/mascotas/{id}: VETERINARIO puede verla")
    void ver_veterinario_permitido() throws Exception {
        get("/api/mascotas/" + mascotaCliente.getId(), tokenVeterinario).andExpect(permitido());
    }

    @Test
    @DisplayName("POST /api/mascotas/propietario/{id}: un cliente registra una mascota a su nombre")
    void registrar_paraSiMismo_permitido() throws Exception {
        post("/api/mascotas/propietario/" + cliente.getId(), tokenCliente, MASCOTA_JSON).andExpect(permitido());
    }

    @Test
    @DisplayName("POST /api/mascotas/propietario/{id}: un cliente NO puede registrar mascotas a nombre de otro (403)")
    void registrar_paraOtro_403() throws Exception {
        post("/api/mascotas/propietario/" + cliente.getId(), tokenOtroCliente, MASCOTA_JSON)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/mascotas/{id}: otro cliente NO puede editarla (403)")
    void editar_otroCliente_403() throws Exception {
        put("/api/mascotas/" + mascotaCliente.getId(), tokenOtroCliente, MASCOTA_JSON)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/mascotas/{id}: ni el dueño ni el VETERINARIO pueden eliminar (403)")
    void eliminar_noAdmin_403() throws Exception {
        delete("/api/mascotas/" + mascotaCliente.getId(), tokenCliente).andExpect(status().isForbidden());
        delete("/api/mascotas/" + mascotaCliente.getId(), tokenVeterinario).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/mascotas/{id}: ADMIN 204")
    void eliminar_admin_204() throws Exception {
        delete("/api/mascotas/" + mascotaCliente.getId(), tokenAdmin).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/mascotas/propietario/{id}: un cliente NO puede listar las mascotas de otro (403)")
    void listarPorPropietario_otroCliente_403() throws Exception {
        get("/api/mascotas/propietario/" + cliente.getId(), tokenOtroCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/mascotas/propietario/{id}: el propio cliente puede listar sus mascotas")
    void listarPorPropietario_propio_permitido() throws Exception {
        get("/api/mascotas/propietario/" + cliente.getId(), tokenCliente).andExpect(permitido());
    }
}
