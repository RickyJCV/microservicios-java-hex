package com.microservicios.pedidos.domain.model;

import java.math.BigDecimal;

/**
 * Record que representa un item dentro de un pedido.
 * Contiene la información del producto y la cantidad solicitada.
 *
 * @param productoId ID del producto
 * @param productoNombre Nombre del producto
 * @param precioUnitario Precio unitario del producto
 * @param cantidad Cantidad solicitada
 */
public record ItemPedido(
        String productoId,
        String productoNombre,
        BigDecimal precioUnitario,
        Integer cantidad
) {
    /**
     * Constructor compacto con validaciones.
     */
    public ItemPedido {
        if (productoId == null || productoId.isBlank()) {
            throw new IllegalArgumentException("El ID del producto es obligatorio");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
    }

    /**
     * Calcula el subtotal del item (precio * cantidad).
     * 
     * Demuestra:
     * - Cálculos con BigDecimal para precisión decimal
     * - Método de utilidad en un Record
     *
     * @return Subtotal del item
     */
    public BigDecimal subtotal() {
        // BigDecimal.multiply para multiplicación precisa de decimales
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /**
     * Método de fábrica para crear un ItemPedido.
     *
     * @param productoId ID del producto
     * @param productoNombre Nombre del producto
     * @param precioUnitario Precio unitario
     * @param cantidad Cantidad solicitada
     * @return Nueva instancia de ItemPedido
     */
    public static ItemPedido crear(String productoId, String productoNombre, 
                                  BigDecimal precioUnitario, Integer cantidad) {
        return new ItemPedido(productoId, productoNombre, precioUnitario, cantidad);
    }
}
