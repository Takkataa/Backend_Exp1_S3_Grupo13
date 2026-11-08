package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Collection;

/**
 * Adaptador que convierte la entidad Usuario en un UserDetails de Spring Security,
 * exponiendo email, contraseña (hash) y el rol como autoridad.
 */
public class UsuarioDetails implements UserDetails {

    // Usuario de la base de datos que se está adaptando.
    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    // Autoridad del usuario. El prefijo ROLE_ permite usar hasRole("ADMIN") en @PreAuthorize.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    // Hash BCrypt almacenado; Spring Security lo compara con la clave ingresada.
    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    // El identificador de inicio de sesión es el email.
    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    // Estados de la cuenta: aquí siempre activa. Podrían ligarse a campos de la entidad (bloqueo, expiración).
    @Override
    public boolean isAccountNonExpired() {
        return true; // Puedes implementar lógica adicional si es necesario
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Puedes implementar lógica adicional si es necesario
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Puedes implementar lógica adicional si es necesario
    }

    @Override
    public boolean isEnabled() {
        return true; // Puedes implementar lógica adicional si es necesario
    }
}
