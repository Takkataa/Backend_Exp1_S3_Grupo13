package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA del programa de fidelización: puntos otorgados a un usuario por compras o servicios (tabla "recompensas").
 */
@Entity
@Table(name = "recompensas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recompensa {

    // Clave primaria autogenerada por la base de datos (AUTO_INCREMENT en MySQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Puntos otorgados.
    private Integer puntos;

    private String descripcion;

    private LocalDateTime fecha;

    // Relaciones
    // Usuario que recibe los puntos.
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
