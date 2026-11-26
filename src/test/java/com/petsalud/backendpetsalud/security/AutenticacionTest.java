package com.petsalud.backendpetsalud.security;

import com.jayway.jsonpath.JsonPath;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.support.SeguridadTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autenticación por usuario y contraseña: login (POST /api/login) y registro público.
 */
class AutenticacionTest extends SeguridadTestBase {

    private String loginJson(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    // ---------- Login ----------

    @Test
    @DisplayName("Login correcto devuelve un JWT de tipo Bearer con el rol del usuario")
    void login_credencialesCorrectas_devuelveToken() throws Exception {
        post("/api/login", null, loginJson("cliente@test.cl", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.email").value("cliente@test.cl"))
                .andExpect(jsonPath("$.rol").value("ROLE_CLIENTE"))
                .andExpect(jsonPath("$.expiraEnMs").value(3600000));
    }

    @Test
    @DisplayName("El token obtenido en el login sirve para acceder a un endpoint protegido")
    void login_tokenObtenido_permiteAcceder() throws Exception {
        String respuesta = post("/api/login", null, loginJson("vet@test.cl", PASSWORD))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(respuesta, "$.token");

        get("/api/productos", token).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta responde 401 con mensaje genérico")
    void login_passwordIncorrecta_401() throws Exception {
        post("/api/login", null, loginJson("cliente@test.cl", "claveEquivocada"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales invalidas"));
    }

    @Test
    @DisplayName("Login con email inexistente responde exactamente lo mismo (no revela qué usuarios existen)")
    void login_emailInexistente_401() throws Exception {
        post("/api/login", null, loginJson("noexiste@test.cl", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales invalidas"));
    }

    // ---------- Registro público ----------

    @Test
    @DisplayName("Registro público: no requiere token, siempre crea CLIENTE aunque se pida ADMIN")
    void registro_ignoraRolEnviado_yCreaCliente() throws Exception {
        String json = """
                {"email": "nuevo@test.cl", "password": "Secreta123!", "nombre": "Nico",
                 "apellido": "Nuevo", "rol": "ADMIN"}
                """;

        post("/api/usuarios/registro", null, json)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.password").doesNotExist()); // el hash nunca se devuelve

        Usuario guardado = usuarioRepository.findByEmail("nuevo@test.cl").orElseThrow();
        assertThat(guardado.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(guardado.getPassword())
                .as("La contraseña debe guardarse cifrada con BCrypt")
                .isNotEqualTo("Secreta123!")
                .startsWith("$2");
    }

    @Test
    @DisplayName("Un usuario recién registrado puede iniciar sesión")
    void registro_luegoLogin_funciona() throws Exception {
        post("/api/usuarios/registro", null, """
                {"email": "registro@test.cl", "password": "Secreta123!", "nombre": "Rita"}
                """).andExpect(status().isOk());

        post("/api/login", null, loginJson("registro@test.cl", "Secreta123!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("Registro con un email ya usado es rechazado")
    void registro_emailDuplicado_esRechazado() {
        // HALLAZGO: no existe un @RestControllerAdvice, por eso la IllegalArgumentException del servicio
        // no se convierte en un 400. En MockMvc llega como excepción; en el servidor real se ve como 500.
        assertThatThrownBy(() -> post("/api/usuarios/registro", null, """
                {"email": "cliente@test.cl", "password": "OtraClave1!", "nombre": "Copia"}
                """))
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("El email ya está registrado");
    }

    // ---------- Cuentas internas (VETERINARIO / ADMIN) ----------

    @Test
    @DisplayName("POST /api/usuarios/interno: un CLIENTE no puede crear cuentas de empleados (403)")
    void crearCuentaInterna_cliente_403() throws Exception {
        post("/api/usuarios/interno", tokenCliente, """
                {"email": "falso@test.cl", "password": "Clave123!", "rol": "ADMIN"}
                """).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/usuarios/interno: ADMIN crea un VETERINARIO")
    void crearCuentaInterna_admin_200() throws Exception {
        post("/api/usuarios/interno", tokenAdmin, """
                {"email": "nuevovet@test.cl", "password": "Clave123!", "nombre": "Vera", "rol": "VETERINARIO"}
                """).andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("VETERINARIO"));
    }
}
