package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.support.SeguridadTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorización en /api/ventas.
 * Regla: un CLIENTE compra y ve solo sus compras; VETERINARIO/ADMIN registran y consultan ventas;
 * el listado completo es solo ADMIN.
 */
class VentaControllerSeguridadTest extends SeguridadTestBase {

    private String productos() {
        return "[" + producto.getId() + "]";
    }

    @Test
    @DisplayName("POST /api/ventas: sin token 401")
    void comprar_sinToken_401() throws Exception {
        post("/api/ventas?usuarioId=" + cliente.getId(), null, productos()).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/ventas: un cliente compra para sí mismo")
    void comprar_paraSiMismo_permitido() throws Exception {
        post("/api/ventas?usuarioId=" + cliente.getId(), tokenCliente, productos()).andExpect(permitido());
    }

    @Test
    @DisplayName("POST /api/ventas: un cliente NO puede registrar compras a nombre de otro (403)")
    void comprar_aNombreDeOtro_403() throws Exception {
        post("/api/ventas?usuarioId=" + cliente.getId(), tokenOtroCliente, productos())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/ventas/{id}: CLIENTE 403 y VETERINARIO pasa la seguridad (404 si no existe)")
    void verPorId_porRol() throws Exception {
        get("/api/ventas/999999", tokenCliente).andExpect(status().isForbidden());
        get("/api/ventas/999999", tokenVeterinario).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/ventas/usuario/{id}: el cliente ve sus compras, otro cliente no")
    void comprasDeUsuario_porDueno() throws Exception {
        get("/api/ventas/usuario/" + cliente.getId(), tokenCliente).andExpect(status().isOk());
        get("/api/ventas/usuario/" + cliente.getId(), tokenOtroCliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/ventas: solo ADMIN (CLIENTE y VETERINARIO 403)")
    void listarTodas_soloAdmin() throws Exception {
        get("/api/ventas", tokenCliente).andExpect(status().isForbidden());
        get("/api/ventas", tokenVeterinario).andExpect(status().isForbidden());
        get("/api/ventas", tokenAdmin).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/ventas: VETERINARIO puede registrar una venta en mostrador")
    void comprar_veterinario_permitido() throws Exception {
        post("/api/ventas?usuarioId=" + cliente.getId(), tokenVeterinario, productos()).andExpect(permitido());
    }

    @Test
    @DisplayName("GET /api/ventas/usuario/{id}: sin token 401")
    void comprasDeUsuario_sinToken_401() throws Exception {
        get("/api/ventas/usuario/" + cliente.getId(), null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/ventas/usuario/{id}: VETERINARIO puede consultar las compras de un cliente")
    void comprasDeUsuario_veterinario_200() throws Exception {
        get("/api/ventas/usuario/" + cliente.getId(), tokenVeterinario).andExpect(status().isOk());
    }
}
