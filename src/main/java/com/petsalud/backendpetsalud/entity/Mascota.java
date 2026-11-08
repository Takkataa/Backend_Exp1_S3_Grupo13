package com.petsalud.backendpetsalud.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Entidad JPA de una mascota (tabla "mascotas"). Pertenece a un usuario (propietario)
 * y tiene un historial de citas y expedientes médicos.
 */
@Entity
@Table(name = "mascotas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {

    // Clave primaria autogenerada por la base de datos (AUTO_INCREMENT en MySQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datos descriptivos de la mascota.
    private String nombre;

    private String especie;

    private String raza;

    private LocalDate fechaNacimiento;

    private String sexo;

    // Relaciones
    // Dueño de la mascota (N a 1). Se usa para verificar que un cliente solo accede a SUS mascotas.
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario propietario;

    // Citas veterinarias de la mascota.
    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<Cita> citas;

    // Historial médico digitalizado (vacunas, diagnósticos y tratamientos).
    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL)
    private List<ExpedienteMedico> expedientesMedicos;
}
