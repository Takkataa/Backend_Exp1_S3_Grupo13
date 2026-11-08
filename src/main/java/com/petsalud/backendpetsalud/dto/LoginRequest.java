package com.petsalud.backendpetsalud.dto;

/**
 * Datos que envía el cliente en POST /api/login.
 * Es un record (clase inmutable): se usa un DTO propio para no exponer la entidad Usuario.
 */
public record LoginRequest(String email, String password) {
}
