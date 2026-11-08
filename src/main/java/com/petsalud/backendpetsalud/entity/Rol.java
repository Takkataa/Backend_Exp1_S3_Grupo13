package com.petsalud.backendpetsalud.entity;

/**
 * Roles del sistema. Equivalencia con los roles del caso PetSalud:
 *  - CLIENTE     = cliente
 *  - VETERINARIO = empleado
 *  - ADMIN       = gerente
 * Spring Security los recibe con prefijo ROLE_ (ver UsuarioDetails), lo que permite usar hasRole("ADMIN").
 */
public enum Rol {
    CLIENTE,
    VETERINARIO,
    ADMIN
}
