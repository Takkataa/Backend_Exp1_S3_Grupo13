package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.support.SeguridadTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Monitoreo de seguridad pedido en la retroalimentación de la Sumativa 1:
 * "registro de logins fallidos y 403".
 *
 * OutputCaptureExtension captura lo que la aplicación escribe en la consola (el log) durante cada prueba,
 * y el parámetro CapturedOutput permite revisarlo.
 */
@ExtendWith(OutputCaptureExtension.class)
class AuditoriaSeguridadTest extends SeguridadTestBase {

    @Test
    @DisplayName("Un login fallido queda en el log con email e IP, pero SIN la contraseña")
    void loginFallido_quedaRegistradoSinPassword(CapturedOutput log) throws Exception {
        post("/api/login", null, """
                {"email": "cliente@test.cl", "password": "ClaveEquivocada999"}
                """).andExpect(status().isUnauthorized());

        assertThat(log.getOut())
                .contains("SEGURIDAD LOGIN_FALLIDO email=cliente@test.cl ip=127.0.0.1")
                .doesNotContain("ClaveEquivocada999");
    }

    @Test
    @DisplayName("Un acceso denegado (403) queda en el log con usuario, método y ruta")
    void accesoDenegado_quedaRegistrado(CapturedOutput log) throws Exception {
        get("/api/mascotas/" + mascotaCliente.getId(), tokenOtroCliente).andExpect(status().isForbidden());

        assertThat(log.getOut()).contains(
                "SEGURIDAD ACCESO_DENEGADO usuario=otro@test.cl GET /api/mascotas/" + mascotaCliente.getId());
    }

    @Test
    @DisplayName("Una solicitud sin token (401) queda en el log con método y ruta")
    void sinToken_quedaRegistrado(CapturedOutput log) throws Exception {
        get("/api/productos", null).andExpect(status().isUnauthorized());

        assertThat(log.getOut()).contains("SEGURIDAD NO_AUTENTICADO GET /api/productos");
    }

    @Test
    @DisplayName("Un email con saltos de línea no puede crear líneas falsas en el log (log injection)")
    void emailConSaltoDeLinea_noFalsificaElLog(CapturedOutput log) throws Exception {
        // En el JSON, \\n es un salto de línea real: el atacante intenta escribir una línea "LOGIN_EXITOSO" falsa.
        post("/api/login", null, """
                {"email": "falso@test.cl\\nSEGURIDAD LOGIN_EXITOSO email=admin@test.cl", "password": "x"}
                """).andExpect(status().isUnauthorized());

        assertThat(log.getOut())
                .contains("LOGIN_FALLIDO email=falso@test.cl_SEGURIDAD LOGIN_EXITOSO email=admin@test.cl")
                .doesNotContain("\nSEGURIDAD LOGIN_EXITOSO email=admin@test.cl");
    }
}
