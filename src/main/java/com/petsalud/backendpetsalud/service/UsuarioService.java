package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de la lógica de negocio de usuarios.
 */
public interface UsuarioService {
    /**
     * Registro público: siempre crea un usuario con rol CLIENTE y contraseña cifrada.
     */
    Usuario registrarUsuario(Usuario usuario);
    /**
     * Creación de cuentas internas (VETERINARIO/ADMIN). Solo se expone a ADMIN en el controlador.
     */
    Usuario crearUsuarioInterno(Usuario usuario);
    /**
     * Busca un usuario por id.
     */
    Optional<Usuario> obtenerUsuarioPorId(Long id);
    /**
     * Busca un usuario por email.
     */
    Optional<Usuario> obtenerUsuarioPorEmail(String email);
    /**
     * Actualiza los datos personales. No modifica email, contraseña ni rol.
     */
    Usuario actualizarUsuario(Long id, Usuario usuario);
    /**
     * Elimina un usuario.
     */
    void eliminarUsuario(Long id);
    /**
     * Lista todos los usuarios.
     */
    List<Usuario> listarUsuarios();
}
