package com.petsalud.backendpetsalud.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Monitoreo de seguridad: deja en el log los eventos que conviene vigilar.
 *
 * - LOGIN_EXITOSO   (INFO): inicio de sesión correcto.
 * - LOGIN_FALLIDO   (WARN): email o contraseña incorrectos. Muchos seguidos = posible fuerza bruta.
 * - NO_AUTENTICADO  (WARN): solicitud sin token o con token inválido/expirado (respuesta 401).
 * - ACCESO_DENEGADO (WARN): usuario autenticado sin permiso para la operación (respuesta 403).
 *
 * Nunca se registran contraseñas ni tokens. Los textos que vienen del cliente se limpian de
 * saltos de línea para que nadie pueda inventar líneas falsas en el log (log injection).
 *
 * Con Docker los eventos se ven con:  docker compose logs app | grep SEGURIDAD
 */
@Component
public class SecurityAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditLogger.class);

    public void loginExitoso(String email, HttpServletRequest request) {
        log.info("SEGURIDAD LOGIN_EXITOSO email={} ip={}", limpiar(email), ip(request));
    }

    public void loginFallido(String email, HttpServletRequest request) {
        log.warn("SEGURIDAD LOGIN_FALLIDO email={} ip={}", limpiar(email), ip(request));
    }

    public void noAutenticado(HttpServletRequest request) {
        log.warn("SEGURIDAD NO_AUTENTICADO {} {} ip={}",
                request.getMethod(), limpiar(request.getRequestURI()), ip(request));
    }

    public void accesoDenegado(HttpServletRequest request) {
        log.warn("SEGURIDAD ACCESO_DENEGADO usuario={} {} {} ip={}",
                usuarioActual(), request.getMethod(), limpiar(request.getRequestURI()), ip(request));
    }

    private static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? limpiar(auth.getName()) : "desconocido";
    }

    private static String ip(HttpServletRequest request) {
        return limpiar(request.getRemoteAddr());
    }

    /** Reemplaza saltos de línea y tabulaciones para evitar líneas falsas en el log. */
    static String limpiar(String valor) {
        if (valor == null) {
            return "-";
        }
        return valor.replaceAll("[\\r\\n\\t]", "_");
    }
}
