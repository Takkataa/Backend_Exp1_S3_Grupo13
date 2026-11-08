package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA de una notificación enviada a un usuario (tabla "notificaciones").
 */
@Entity
@Table(name = "notificaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    // Clave primaria autogenerada por la base de datos (AUTO_INCREMENT en MySQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mensaje;

    private String tipo; // Ejemplos: "Promoción", "Recordatorio", "Evento"

    private LocalDateTime fecha;

    // Indica si el usuario ya la leyó.
    private Boolean leida;

    // Relaciones
    // Usuario destinatario.
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
