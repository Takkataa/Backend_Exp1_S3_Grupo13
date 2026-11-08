package com.petsalud.backendpetsalud.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que valida el token JWT del header "Authorization: Bearer <token>"
 * antes de procesar cada solicitud.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UsuarioDetailsService usuarioDetailsService) {
        this.jwtUtil = jwtUtil;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Paso 1: leer el header Authorization (formato esperado: "Bearer <token>").
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Se quita el prefijo "Bearer " (7 caracteres) para quedarse solo con el token.
            String token = header.substring(7);
            try {
                // Paso 2: extraer el email del token (esto también verifica la firma y la expiración).
                String email = jwtUtil.extractUsername(token);
                // Paso 3: cargar al usuario desde la base de datos (así se detectan usuarios eliminados).
                UserDetails userDetails = usuarioDetailsService.loadUserByUsername(email);

                // Paso 4: si el token es válido, se registra al usuario como autenticado con sus roles.
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                // Token invalido, expirado o usuario inexistente: no se autentica.
                // La solicitud continua sin autenticacion y sera rechazada con 401.
                SecurityContextHolder.clearContext();
            }
        }

        // Se continúa con el resto de filtros. Si no hubo autenticación, Spring Security responderá 401 en rutas protegidas.
        filterChain.doFilter(request, response);
    }
}
