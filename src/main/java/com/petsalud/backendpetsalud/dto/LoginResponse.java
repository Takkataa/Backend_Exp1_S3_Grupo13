package com.petsalud.backendpetsalud.dto;

/**
 * Respuesta del login: contiene el token JWT que el cliente debe enviar en cada solicitud
 * como header "Authorization: Bearer <token>".
 */
/**
 * token: JWT firmado | tipo: siempre "Bearer" | email y rol: datos del usuario | expiraEnMs: vigencia del token en milisegundos.
 */
public record LoginResponse(String token, String tipo, String email, String rol, long expiraEnMs) {
}
