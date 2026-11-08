package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Producto;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.repository.ProductoRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import com.petsalud.backendpetsalud.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio de ventas: valida productos y stock, descuenta inventario y calcula el total.
 */
@Service
public class VentaServiceImpl implements VentaService {

    // Repositorio de ventas.
    @Autowired
    private VentaRepository ventaRepository;

    // Repositorio de usuarios (comprador).
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Repositorio de productos (stock y precios).
    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public Venta realizarVenta(Long usuarioId, List<Long> productosIds) {
        // Se valida que el comprador exista.
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Se cargan los productos; si falta alguno se rechaza la venta.
        List<Producto> productos = productoRepository.findAllById(productosIds);

        if (productos.size() != productosIds.size()) {
            throw new IllegalArgumentException("Algunos productos no fueron encontrados");
        }

        // Acumulador del total. Se usa BigDecimal para no perder precisión en dinero.
        BigDecimal total = BigDecimal.ZERO;
        for (Producto producto : productos) {
            // Se rechaza el producto sin stock.
            if (producto.getStock() <= 0) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no tiene stock disponible");
            }
            // Restar stock
            // Se descuenta una unidad del inventario por cada producto vendido.
            producto.setStock(producto.getStock() - 1);
            productoRepository.save(producto);

            total = total.add(producto.getPrecio());
        }

        // Se crea la venta con la fecha actual del servidor.
        Venta venta = Venta.builder()
                .usuario(usuario)
                .productos(productos)
                .total(total)
                .fecha(LocalDateTime.now())
                .build();

        return ventaRepository.save(venta);
    }

    @Override
    public Optional<Venta> obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id);
    }

    @Override
    public List<Venta> listarVentasPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public List<Venta> listarTodasLasVentas() {
        return ventaRepository.findAll();
    }
}

