package com.petsalud.backendpetsalud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utilidad para generar y validar tokens JWT firmados con HMAC-SHA.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secretBase64,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        // Sin clave no se arranca: es preferible que la aplicación falle a que firme tokens
        // con una clave conocida (antes había un valor de respaldo en application.properties).
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException(
                    "Falta la variable de entorno JWT_SECRET. Genere una clave con 'openssl rand -base64 64' "
                    + "y defínala antes de iniciar la aplicación (ver README, sección Configuración).");
        }
        byte[] claveDecodificada;
        try {
            claveDecodificada = Decoders.BASE64.decode(secretBase64.trim());
        } catch (DecodingException e) {
            throw new IllegalStateException("JWT_SECRET no es un texto Base64 válido. "
                    + "Genere una clave con 'openssl rand -base64 64'.", e);
        }
        // La clave debe tener al menos 256 bits (32 bytes); si no, jjwt lanza WeakKeyException.
        this.key = Keys.hmacShaKeyFor(claveDecodificada);
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        Date ahora = new Date();
        String rol = userDetails.getAuthorities().iterator().next().getAuthority();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /** Un token es valido si su firma es correcta, no ha expirado y pertenece al usuario. */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
