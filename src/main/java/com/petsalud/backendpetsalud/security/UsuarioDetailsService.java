package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Puente entre Spring Security y la base de datos.
 * Spring Security llama a loadUserByUsername() durante el login y al validar cada token JWT.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    // Repositorio para buscar al usuario en MySQL.
    @Autowired
    private UsuarioRepository usuarioRepository;

    // El "username" en este sistema es el email.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Si no existe, se lanza UsernameNotFoundException (que se traduce en login fallido).
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
        // Se adapta la entidad Usuario al formato UserDetails que entiende Spring Security.
        return new UsuarioDetails(usuario);
    }
}
