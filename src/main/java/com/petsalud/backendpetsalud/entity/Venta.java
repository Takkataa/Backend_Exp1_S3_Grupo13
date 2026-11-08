package com.petsalud.backendpetsalud.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad JPA de una venta (tabla "ventas"): quién compró, qué productos y por cuánto.
 */
@Entity
@Table(name = "ventas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    // Clave primaria autogenerada por la base de datos (AUTO_INCREMENT en MySQL).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    // Suma de los precios de los productos vendidos.
    private BigDecimal total;

    // Relaciones
    // Cliente que realizó la compra.
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToMany
    @JoinTable(
            name = "venta_productos",
            joinColumns = @JoinColumn(name = "venta_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    // Productos de la venta (N a N). La tabla intermedia "venta_productos" se crea automáticamente.
    private List<Producto> productos;
}
