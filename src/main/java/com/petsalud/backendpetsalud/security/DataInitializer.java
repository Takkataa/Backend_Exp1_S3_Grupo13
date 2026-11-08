package com.petsalud.backendpetsalud.security;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el primer usuario ADMIN (gerente) al arrancar la aplicación.
 *
 * Es necesario porque el registro público solo crea clientes y la creación de cuentas internas
 * (POST /api/usuarios/interno) exige estar autenticado como ADMIN: sin un primer ADMIN nadie
 * podría crear empleados. Los datos vienen de las variables ADMIN_EMAIL y ADMIN_PASSWORD.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        // Sin contraseña configurada no se crea nada (evita dejar un ADMIN con clave conocida)
        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_PASSWORD no definido: no se crea administrador inicial.");
            return;
        }
        // Si ya existe, no se toca (así no se pisan cambios hechos después)
        if (usuarioRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }
        Usuario admin = Usuario.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword)) // siempre cifrada con BCrypt
                .nombre("Administrador")
                .apellido("PetSalud")
                .rol(Rol.ADMIN)
                .build();
        usuarioRepository.save(admin);
        log.info("Administrador inicial creado: {}", adminEmail);
    }
}
