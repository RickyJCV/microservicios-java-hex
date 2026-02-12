package com.microservicios.pedidos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record que representa un Pedido en el dominio.
 * Un pedido contiene información del cliente y una lista de items solicitados.
 *
 * @param id Identificador único del pedido
 * @param clienteId Identificador del cliente que realizó el pedido
 * @param clienteNombre Nombre del cliente
 * @param items Lista de items del pedido
 * @param total Monto total del pedido
 * @param estado Estado actual del pedido
 * @param fechaCreacion Fecha de creación del pedido
 * @param fechaActualizacion Fecha de última actualización
 */
public record Pedido(
        String id,
        String clienteId,
        String clienteNombre,
        List<ItemPedido> items,
        BigDecimal total,
        EstadoPedido estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
    /**
     * Constructor compacto con validaciones de negocio.
     */
    public Pedido {
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un item");
        }
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total debe ser mayor que cero");
        }
    }

    /**
     * Método de fábrica para crear un nuevo pedido.
     * Calcula automáticamente el total basándose en los items.
     *
     * @param clienteId ID del cliente
     * @param clienteNombre Nombre del cliente
     * @param items Lista de items del pedido
     * @return Nueva instancia de Pedido
     */
    public static Pedido crear(String clienteId, String clienteNombre, List<ItemPedido> items) {
        // Calcular el total usando streams y reduce
        BigDecimal total = items.stream()
                .map(ItemPedido::subtotal) // Obtener subtotal de cada item
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sumar todos los subtotales

        return new Pedido(
                null, // El ID se genera en la capa de infraestructura
                clienteId,
                clienteNombre,
                items,
                total,
                EstadoPedido.PENDIENTE, // Estado inicial
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * Actualiza el estado del pedido.
     * Demuestra inmutabilidad - retorna una nueva instancia.
     *
     * @param nuevoEstado Nuevo estado del pedido
     * @return Nueva instancia con el estado actualizado
     */
    public Pedido cambiarEstado(EstadoPedido nuevoEstado) {
        return new Pedido(
                this.id,
                this.clienteId,
                this.clienteNombre,
                this.items,
                this.total,
                nuevoEstado,
                this.fechaCreacion,
                LocalDateTime.now() // Actualizar fecha de modificación
        );
    }

    /**
     * Verifica si el pedido puede ser cancelado.
     *
     * @return true si el pedido puede cancelarse
     */
    public boolean puedeCancelarse() {
        return this.estado == EstadoPedido.PENDIENTE || 
               this.estado == EstadoPedido.CONFIRMADO;
    }

    /**
     * Obtiene la cantidad total de items en el pedido.
     *
     * @return Suma de cantidades de todos los items
     */
    public int cantidadTotalItems() {
        return items.stream()
                .mapToInt(ItemPedido::cantidad)
                .sum();
    }
}
