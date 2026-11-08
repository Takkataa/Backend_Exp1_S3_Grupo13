package com.petsalud.backendpetsalud.repository;

import com.petsalud.backendpetsalud.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acceso a datos de usuarios. Spring Data JPA implementa la interfaz automáticamente
 * a partir del nombre de los métodos (no se escribe SQL). Al usar consultas derivadas con parámetros,
 * los valores viajan como parámetros preparados, lo que previene la inyección SQL.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    /**
     * Busca un usuario por su email. Lo usa el login (UsuarioDetailsService) y las comprobaciones de propiedad.
     */
    Optional<Usuario> findByEmail(String email);
}
