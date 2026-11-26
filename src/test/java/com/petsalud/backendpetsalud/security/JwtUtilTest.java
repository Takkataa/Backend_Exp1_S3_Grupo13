package com.petsalud.backendpetsalud.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de JwtUtil (sin Spring ni base de datos): generación y validación de tokens.
 */
class JwtUtilTest {

    // Claves Base64 de 512 bits, solo para pruebas.
    private static final String CLAVE = "Nd2r7ufFQX9lMmD9J8meWhgPXM5pJJUnSjUUeSfNgXzl9YRqnj0G6FE7Ve3KjNAoUXUVmqzXVWKnm10urVBTig==";
    private static final String OTRA_CLAVE = "+EYgdwMvPMx7jgJhbUXL15dXuuUvhxbp39J+y/qGk6UUKBj1GXLCdNombthNlbIyYA+kTZtLj+YMcG26HbeEmA==";
    private static final long UNA_HORA = 3_600_000L;

    private JwtUtil jwtUtil;
    private UserDetails cliente;
    private UserDetails veterinario;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(CLAVE, UNA_HORA);
        cliente = User.withUsername("cliente@test.cl").password("x").roles("CLIENTE").build();
        veterinario = User.withUsername("vet@test.cl").password("x").roles("VETERINARIO").build();
    }

    @Test
    @DisplayName("El token generado contiene el email del usuario como 'subject'")
    void generarToken_contieneEmailDelUsuario() {
        String token = jwtUtil.generateToken(cliente);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "Un JWT tiene 3 partes: cabecera.payload.firma");
        assertEquals("cliente@test.cl", jwtUtil.extractUsername(token));
    }

    @Test
    @DisplayName("El token incluye el rol del usuario")
    void generarToken_incluyeRol() {
        String token = jwtUtil.generateToken(cliente);
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);

        assertTrue(payload.contains("\"rol\":\"ROLE_CLIENTE\""), "payload: " + payload);
    }

    @Test
    @DisplayName("Un token es válido para el usuario al que pertenece")
    void validarToken_mismoUsuario_esValido() {
        String token = jwtUtil.generateToken(cliente);

        assertTrue(jwtUtil.validateToken(token, cliente));
    }

    @Test
    @DisplayName("Un token NO es válido para otro usuario")
    void validarToken_otroUsuario_esInvalido() {
        String tokenCliente = jwtUtil.generateToken(cliente);

        assertFalse(jwtUtil.validateToken(tokenCliente, veterinario));
    }

    @Test
    @DisplayName("Un token expirado se rechaza")
    void tokenExpirado_esRechazado() {
        JwtUtil jwtQueExpira = new JwtUtil(CLAVE, -1_000L); // expiró hace 1 segundo
        String tokenVencido = jwtQueExpira.generateToken(cliente);

        assertFalse(jwtUtil.validateToken(tokenVencido, cliente));
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.extractUsername(tokenVencido));
    }

    @Test
    @DisplayName("Un token firmado con otra clave se rechaza (no se puede falsificar)")
    void tokenFirmadoConOtraClave_esRechazado() {
        String tokenFalso = new JwtUtil(OTRA_CLAVE, UNA_HORA).generateToken(cliente);

        assertFalse(jwtUtil.validateToken(tokenFalso, cliente));
        assertThrows(JwtException.class, () -> jwtUtil.extractUsername(tokenFalso));
    }

    @Test
    @DisplayName("Un token con la firma alterada se rechaza")
    void tokenAlterado_esRechazado() {
        String[] partes = jwtUtil.generateToken(cliente).split("\\.");
        char primero = partes[2].charAt(0);
        String firmaAlterada = (primero == 'A' ? 'B' : 'A') + partes[2].substring(1);
        String tokenAlterado = partes[0] + "." + partes[1] + "." + firmaAlterada;

        assertFalse(jwtUtil.validateToken(tokenAlterado, cliente));
    }

    @Test
    @DisplayName("Un texto que no es JWT se rechaza")
    void tokenMalformado_esRechazado() {
        assertFalse(jwtUtil.validateToken("esto.no-es.un-token", cliente));
        assertThrows(JwtException.class, () -> jwtUtil.extractUsername("esto.no-es.un-token"));
    }

    @Test
    @DisplayName("Una clave de menos de 256 bits no se acepta")
    void claveDebil_lanzaExcepcion() {
        String claveCorta = Base64.getEncoder().encodeToString("corta".getBytes(StandardCharsets.UTF_8));

        assertThrows(WeakKeyException.class, () -> new JwtUtil(claveCorta, UNA_HORA));
    }

    @Test
    @DisplayName("La duración configurada del token se expone correctamente")
    void getExpirationMs_devuelveValorConfigurado() {
        assertEquals(UNA_HORA, jwtUtil.getExpirationMs());
    }
}
