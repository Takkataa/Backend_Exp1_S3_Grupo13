package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA de un registro del historial clínico de una mascota (tabla "expedientes_medicos").
 * Contiene datos sensibles, por eso solo VETERINARIO/ADMIN pueden crearlos y el cliente únicamente
 * puede ver los de sus propias mascotas.
 */
@Entity
@Table(name = "expedientes_medicos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpedienteMedico {

    // Clave primaria autogenerada por la base de datos (AUTO_INCREMENT en MySQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fecha del registro (se asigna automáticamente al guardarlo).
    private LocalDateTime fecha;

    private String descripcion;

    // Diagnóstico y tratamiento indicados por el veterinario.
    private String diagnostico;

    private String tratamiento;

    // Relaciones
    // Mascota a la que pertenece el registro.
    @ManyToOne
    @JoinColumn(name = "mascota_id")
    private Mascota mascota;

    // Veterinario que lo redactó.
    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private Usuario veterinario;
}
