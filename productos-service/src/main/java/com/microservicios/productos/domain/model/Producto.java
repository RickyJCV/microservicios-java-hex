package com.microservicios.productos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record que representa un Producto en el dominio.
 * Los Records son una característica de Java 14+ que proporciona una forma concisa de crear clases inmutables.
 * Automáticamente generan constructor, getters, equals(), hashCode() y toString().
 *
 * @param id Identificador único del producto
 * @param nombre Nombre del producto
 * @param descripcion Descripción detallada del producto
 * @param precio Precio del producto
 * @param stock Cantidad disponible en inventario
 * @param categoria Categoría a la que pertenece el producto
 * @param activo Indica si el producto está activo para la venta
 * @param fechaCreacion Fecha y hora de creación del producto
 * @param fechaActualizacion Fecha y hora de última actualización
 */
public record Producto(
        String id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String categoria,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    /**
     * Constructor compacto que valida los datos del producto.
     * Se ejecuta antes del constructor canónico generado automáticamente.
     */
    public Producto {
        // Validaciones de negocio
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a cero");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    /**
     * Método de fábrica para crear un nuevo producto.
     * Útil para establecer valores por defecto.
     *
     * @param nombre Nombre del producto
     * @param descripcion Descripción del producto
     * @param precio Precio del producto
     * @param stock Stock inicial
     * @param categoria Categoría del producto
     * @return Nueva instancia de Producto
     */
    public static Producto crear(String nombre, String descripcion, BigDecimal precio, 
                                 Integer stock, String categoria) {
        return new Producto(
                null, // El ID se genera en la capa de infraestructura
                nombre,
                descripcion,
                precio,
                stock,
                categoria,
                true, // Por defecto, los productos se crean activos
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * Crea una copia del producto con un nuevo stock.
     * Ejemplo de método que aprovecha la inmutabilidad de los Records.
     *
     * @param nuevoStock Nuevo valor de stock
     * @return Nueva instancia con el stock actualizado
     */
    public Producto actualizarStock(Integer nuevoStock) {
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        return new Producto(
                this.id,
                this.nombre,
                this.descripcion,
                this.precio,
                nuevoStock,
                this.categoria,
                this.activo,
                this.fechaCreacion,
                LocalDateTime.now() // Actualiza la fecha de modificación
        );
    }

    /**
     * Verifica si hay stock disponible.
     *
     * @param cantidad Cantidad solicitada
     * @return true si hay stock suficiente
     */
    public boolean tieneStockDisponible(Integer cantidad) {
        return this.stock >= cantidad && this.activo;
    }
}
