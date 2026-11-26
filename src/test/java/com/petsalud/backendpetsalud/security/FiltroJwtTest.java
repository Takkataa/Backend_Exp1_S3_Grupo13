package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.support.SeguridadTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Filtro JWT y configuración general de SecurityConfig:
 * qué pasa con solicitudes sin token, con tokens inválidos y qué cabeceras de seguridad se envían.
 */
class FiltroJwtTest extends SeguridadTestBase {

    private static final String ENDPOINT_PROTEGIDO = "/api/productos";

    // Misma clave que usa la aplicación en las pruebas (src/test/resources/application.properties).
    @Value("${jwt.secret}")
    private String claveDeLaApp;

    @Test
    @DisplayName("Sin token: 401")
    void sinToken_401() throws Exception {
        get(ENDPOINT_PROTEGIDO, null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token válido: acceso concedido (200)")
    void tokenValido_200() throws Exception {
        get(ENDPOINT_PROTEGIDO, tokenCliente).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Texto que no es un JWT: 401")
    void tokenMalformado_401() throws Exception {
        get(ENDPOINT_PROTEGIDO, "abc.def.ghi").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token firmado con otra clave (falsificado): 401")
    void tokenFalsificado_401() throws Exception {
        String claveDelAtacante = "+EYgdwMvPMx7jgJhbUXL15dXuuUvhxbp39J+y/qGk6UUKBj1GXLCdNombthNlbIyYA+kTZtLj+YMcG26HbeEmA==";
        String tokenFalso = new JwtUtil(claveDelAtacante, 3_600_000L).generateToken(new UsuarioDetails(admin));

        get(ENDPOINT_PROTEGIDO, tokenFalso).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token expirado: 401")
    void tokenExpirado_401() throws Exception {
        String tokenVencido = new JwtUtil(claveDeLaApp, -1_000L).generateToken(new UsuarioDetails(cliente));

        get(ENDPOINT_PROTEGIDO, tokenVencido).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token de un usuario que fue eliminado: 401")
    void tokenDeUsuarioEliminado_401() throws Exception {
        Usuario temporal = crearUsuario("temporal@test.cl", "Tomas", Rol.CLIENTE);
        String token = tokenDe(temporal);
        usuarioRepository.delete(temporal);

        get(ENDPOINT_PROTEGIDO, token).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token sin el prefijo 'Bearer ': 401")
    void tokenSinPrefijoBearer_401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT_PROTEGIDO).header(HttpHeaders.AUTHORIZATION, tokenCliente))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HTTP Basic está desactivado: usuario y contraseña correctos sin JWT dan 401")
    void httpBasicDesactivado_401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT_PROTEGIDO).with(httpBasic("cliente@test.cl", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API sin sesiones (stateless): no se crea cookie de sesión")
    void sinSesion_noDevuelveCookie() throws Exception {
        get(ENDPOINT_PROTEGIDO, tokenCliente)
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("CSRF desactivado: un POST con token válido no necesita token CSRF")
    void postConJwt_noRequiereCsrf() throws Exception {
        post("/api/productos", tokenVeterinario, """
                {"nombre": "Collar", "precio": 4990, "stock": 5, "categoria": "Accesorios"}
                """).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Las respuestas incluyen cabeceras contra XSS y clickjacking")
    void respuestas_incluyenCabecerasDeSeguridad() throws Exception {
        get(ENDPOINT_PROTEGIDO, tokenCliente)
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }
}
