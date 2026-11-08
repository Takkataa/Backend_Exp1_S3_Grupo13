package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entidad JPA de un producto o medicamento del inventario (tabla "productos").
 */
@Entity
@Table(name = "productos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    // Clave primaria autogenerada por la base de datos (AUTO_INCREMENT en MySQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String descripcion;

    // BigDecimal evita errores de redondeo propios de double/float al manejar dinero.
    private BigDecimal precio;

    // Unidades disponibles; se descuenta al realizar una venta.
    private Integer stock;

    // Categoría (alimentos, accesorios, medicamentos, etc.).
    private String categoria;
}

