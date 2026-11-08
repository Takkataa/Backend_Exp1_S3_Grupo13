package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.LoginRequest;
import com.petsalud.backendpetsalud.dto.LoginResponse;
import com.petsalud.backendpetsalud.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de inicio de sesión. Es público (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    // Valida las credenciales contra la base de datos usando BCrypt.
    @Autowired
    private AuthenticationManager authenticationManager;

    // Genera el token tras un login exitoso.
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * POST /api/login
Recibe email y contraseña; si son correctos devuelve un token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Si las credenciales son inválidas, authenticate() lanza AuthenticationException.
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            UserDetails user = (UserDetails) auth.getPrincipal();
            // Credenciales correctas: se emite el token.
            String token = jwtUtil.generateToken(user);
            String rol = user.getAuthorities().iterator().next().getAuthority();
            return ResponseEntity.ok(new LoginResponse(token, "Bearer", user.getUsername(), rol,
                    jwtUtil.getExpirationMs()));
        } catch (AuthenticationException e) {
            // Mensaje generico: no revela si fallo el email o la contrasena
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales invalidas");
        }
    }
}
