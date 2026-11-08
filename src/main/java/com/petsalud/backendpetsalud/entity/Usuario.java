package com.petsalud.backendpetsalud.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entidad JPA que representa a cualquier persona con cuenta en el sistema (tabla "usuarios").
 * Un mismo modelo cubre a clientes, veterinarios (empleados) y administradores (gerentes);
 * lo que los diferencia es el campo "rol", que Spring Security usa para autorizar.
 * Lombok genera getters, setters, constructores y builder, por eso no se ven en el código.
 */
@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    // Clave primaria autogenerada por la base de datos (AUTO_INCREMENT en MySQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email: es el "nombre de usuario" para iniciar sesión. Debe ser único y obligatorio.
    @Column(nullable = false, unique = true)
    private String email;

    // Contraseña. Se guarda siempre como hash BCrypt (ver UsuarioServiceImpl).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // nunca se devuelve el hash en las respuestas
    @Column(nullable = false)
    private String password;

    // Datos personales del perfil.
    private String nombre;

    private String apellido;

    private String telefono;

    private String direccion;

    // Rol del usuario (CLIENTE, VETERINARIO o ADMIN). Se guarda como texto para que no cambie si se reordena el enum.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    // Relaciones
    // Mascotas de las que es propietario (relación 1 a N). cascade ALL: las operaciones sobre el usuario se propagan.
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL)
    private List<Mascota> mascotas;

    // Compras realizadas por el usuario.
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Venta> ventas;

    // Puntos de fidelización acumulados.
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Recompensa> recompensas;

    // Notificaciones recibidas (promociones, recordatorios, eventos).
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Notificacion> notificaciones;
}