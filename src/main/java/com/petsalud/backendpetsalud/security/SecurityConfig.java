package com.petsalud.backendpetsalud.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de Spring Security: define qué rutas son públicas, cómo se autentica
 * (JWT sin sesiones), el cifrado de contraseñas y el filtro que valida los tokens.
 */
// @EnableMethodSecurity activa @PreAuthorize en los controladores.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en los controladores
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtUtil jwtUtil;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService, JwtUtil jwtUtil) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.jwtUtil = jwtUtil;
    }

    // Cadena de filtros de seguridad: se ejecuta en cada solicitud HTTP.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Filtro propio que lee y valida el token JWT.
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, usuarioDetailsService);

        http
                // API stateless: el token viaja en el header Authorization (no en cookies),
                // por lo que el navegador no lo adjunta automaticamente y no hay riesgo CSRF.
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas: registro y login. Todo lo demás exige autenticación.
                        .requestMatchers("/api/usuarios/registro", "/api/login").permitAll()
                        .anyRequest().authenticated()
                )
                // STATELESS: el servidor no guarda sesión; cada solicitud debe traer su token.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Se desactivan Basic Auth y formulario: la única vía de autenticación es el JWT.
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // Sin token o token invalido -> 401 (rol insuficiente -> 403 por defecto)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                // Cabeceras para mitigar XSS / clickjacking en las respuestas de la API
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                )
                // El filtro JWT se ejecuta antes del filtro estándar de usuario/contraseña.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Codificador BCrypt: aplica sal aleatoria y es lento a propósito, lo que dificulta ataques de fuerza bruta.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Gestor de autenticación que usa AuthController para validar email y contraseña.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
