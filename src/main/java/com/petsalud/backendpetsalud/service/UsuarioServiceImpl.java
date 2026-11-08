package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio de usuarios.
 * Aspectos de seguridad: las contraseñas se cifran con BCrypt antes de guardarse y el registro
 * público nunca permite elegir el rol (evita que alguien se registre como ADMIN).
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    // Acceso a la tabla de usuarios.
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Codificador BCrypt definido en SecurityConfig.
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        // El registro publico SIEMPRE crea clientes (evita escalada de privilegios)
        usuario.setRol(Rol.CLIENTE);
        // Reutiliza la validación de email duplicado y el cifrado de contraseña.
        return guardarUsuario(usuario);
    }

    @Override
    public Usuario crearUsuarioInterno(Usuario usuario) {
        // Solo ADMIN llega aqui (ver UsuarioController): permite crear VETERINARIO / ADMIN
        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        return guardarUsuario(usuario);
    }

    private Usuario guardarUsuario(Usuario usuario) {
        // Verificar si el email ya está registrado
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        // Contraseña almacenada con hash BCrypt, nunca en texto plano
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    // Consulta simple por clave primaria.
    @Override
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setApellido(usuarioActualizado.getApellido());
            usuario.setTelefono(usuarioActualizado.getTelefono());
            usuario.setDireccion(usuarioActualizado.getDireccion());
            // No actualizar email ni password aquí por seguridad
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    // Elimina por id (solo ADMIN llega aquí, ver UsuarioController).
    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
