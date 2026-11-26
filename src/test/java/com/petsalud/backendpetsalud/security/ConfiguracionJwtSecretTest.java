package com.petsalud.backendpetsalud.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Corrección pedida en la retroalimentación de la Sumativa 1:
 * "Eliminen el valor de respaldo y hagan fallar el arranque si falta JWT_SECRET".
 *
 * ApplicationContextRunner levanta un mini contexto de Spring solo con JwtUtil, para comprobar
 * qué pasa al arrancar con distintas configuraciones sin levantar la aplicación completa.
 */
class ConfiguracionJwtSecretTest {

    private static final String CLAVE_VALIDA =
            "Nd2r7ufFQX9lMmD9J8meWhgPXM5pJJUnSjUUeSfNgXzl9YRqnj0G6FE7Ve3KjNAoUXUVmqzXVWKnm10urVBTig==";

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(JwtUtil.class)
            .withPropertyValues("jwt.expiration-ms=3600000");

    @Test
    @DisplayName("Sin JWT_SECRET la aplicación NO arranca y el error explica qué falta")
    void sinJwtSecret_noArranca() {
        contexto.withPropertyValues("jwt.secret=")
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure())
                            .hasStackTraceContaining("Falta la variable de entorno JWT_SECRET");
                });
    }

    @Test
    @DisplayName("Con un JWT_SECRET que no es Base64 la aplicación NO arranca")
    void jwtSecretNoBase64_noArranca() {
        contexto.withPropertyValues("jwt.secret=PEGAR_AQUI_LA_CLAVE_GENERADA") // el texto de .env.example
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(ctx.getStartupFailure())
                            .hasStackTraceContaining("JWT_SECRET no es un texto Base64 válido");
                });
    }

    @Test
    @DisplayName("Con un JWT_SECRET válido la aplicación arranca normalmente")
    void jwtSecretValido_arranca() {
        contexto.withPropertyValues("jwt.secret=" + CLAVE_VALIDA)
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).hasSingleBean(JwtUtil.class);
                });
    }

    @Test
    @DisplayName("application.properties ya no tiene una clave JWT de respaldo escrita en el código")
    void applicationProperties_sinClaveDeRespaldo() throws Exception {
        String lineaJwt = Files.readAllLines(
                        Path.of("src/main/resources/application.properties"), StandardCharsets.UTF_8)
                .stream()
                .filter(linea -> linea.startsWith("jwt.secret="))
                .findFirst()
                .orElseThrow();

        assertThat(lineaJwt).isEqualTo("jwt.secret=${JWT_SECRET:}");
    }
}
