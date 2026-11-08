package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA de una cita veterinaria (tabla "citas"): une a una mascota con un veterinario en una fecha y hora.
 */
@Entity
@Table(name = "citas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    // Clave primaria autogenerada por la base de datos (AUTO_INCREMENT en MySQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fecha y hora programadas.
    private LocalDateTime fechaHora;

    private String estado; // Ejemplos: "Agendada", "Cancelada", "Completada"

    private String motivo;

    // Relaciones
    // Mascota que será atendida.
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    // Veterinario (usuario con rol VETERINARIO) que realizará la atención.
    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private Usuario veterinario;
}
