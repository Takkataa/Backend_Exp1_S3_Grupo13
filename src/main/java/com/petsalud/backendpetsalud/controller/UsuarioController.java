package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST de usuarios (/api/usuarios).
 * La autorización se define con @PreAuthorize sobre cada método (requiere @EnableMethodSecurity en SecurityConfig).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    // Servicio con la lógica de negocio.
    @Autowired
    private UsuarioService usuarioService;

    // Solo el gerente (ADMIN) puede crear cuentas de empleados (VETERINARIO) u otros ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/interno")
    public ResponseEntity<Usuario> crearUsuarioInterno(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.crearUsuarioInterno(usuario));
    }

    /**
     * POST /api/usuarios/registro
     * Registro público de clientes. Sin autenticación (permitAll). El rol siempre queda como CLIENTE.
     */
    @PreAuthorize("permitAll()")
    @PostMapping("/registro")
    public ResponseEntity<Usuario> registrarUsuario(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        return ResponseEntity.ok(nuevoUsuario);
    }

    /**
     * GET /api/usuarios/{id}
     * Seguridad: ADMIN, o el propio usuario consultando su perfil.
     */
    @PreAuthorize("hasRole('ADMIN') or @authz.esUsuario(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/usuarios/{id}
     * Seguridad: ADMIN, o el propio usuario editando su perfil.
     */
    @PreAuthorize("hasRole('ADMIN') or @authz.esUsuario(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }

    /**
     * DELETE /api/usuarios/{id}
     * Seguridad: solo ADMIN.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/usuarios
     * Seguridad: solo ADMIN (los datos personales de todos los clientes son sensibles).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
}
