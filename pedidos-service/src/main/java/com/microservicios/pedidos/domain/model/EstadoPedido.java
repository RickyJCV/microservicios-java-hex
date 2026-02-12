package com.microservicios.pedidos.domain.model;

/**
 * Enumeración que representa los posibles estados de un pedido.
 * 
 * Los enums en Java son type-safe y pueden tener métodos y propiedades.
 * Son ideales para representar un conjunto fijo de valores como estados.
 */
public enum EstadoPedido {
    /**
     * Pedido recién creado, esperando confirmación.
     */
    PENDIENTE("Pendiente", "El pedido ha sido creado y está pendiente de confirmación"),
    
    /**
     * Pedido confirmado, listo para procesarse.
     */
    CONFIRMADO("Confirmado", "El pedido ha sido confirmado y está siendo procesado"),
    
    /**
     * Pedido en proceso de preparación.
     */
    EN_PREPARACION("En Preparación", "El pedido está siendo preparado"),
    
    /**
     * Pedido enviado al cliente.
     */
    ENVIADO("Enviado", "El pedido ha sido enviado"),
    
    /**
     * Pedido entregado exitosamente.
     */
    ENTREGADO("Entregado", "El pedido ha sido entregado al cliente"),
    
    /**
     * Pedido cancelado por el cliente o el sistema.
     */
    CANCELADO("Cancelado", "El pedido ha sido cancelado"),
    
    /**
     * Pedido rechazado (por falta de stock, pago fallido, etc.).
     */
    RECHAZADO("Rechazado", "El pedido ha sido rechazado");

    // Propiedades del enum
    private final String nombre;
    private final String descripcion;

    /**
     * Constructor del enum.
     * Los constructores de enums son siempre privados.
     *
     * @param nombre Nombre legible del estado
     * @param descripcion Descripción del estado
     */
    EstadoPedido(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el nombre del estado.
     * Getter personalizado para enum.
     *
     * @return Nombre del estado
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la descripción del estado.
     *
     * @return Descripción del estado
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si el estado es final (no puede cambiar).
     * Demuestra lógica de negocio en enums.
     *
     * @return true si el estado es final
     */
    public boolean esFinal() {
        return this == ENTREGADO || this == CANCELADO || this == RECHAZADO;
    }

    /**
     * Verifica si el pedido puede transicionar a otro estado.
     * Implementa reglas de transición de estados.
     *
     * @param nuevoEstado Estado destino
     * @return true si la transición es válida
     */
    public boolean puedeTransicionarA(EstadoPedido nuevoEstado) {
        // Si ya está en un estado final, no puede cambiar
        if (this.esFinal()) {
            return false;
        }

        // Definir transiciones válidas usando switch expressions (Java 14+)
        return switch (this) {
            case PENDIENTE -> 
                nuevoEstado == CONFIRMADO || nuevoEstado == CANCELADO || nuevoEstado == RECHAZADO;
            case CONFIRMADO -> 
                nuevoEstado == EN_PREPARACION || nuevoEstado == CANCELADO;
            case EN_PREPARACION -> 
                nuevoEstado == ENVIADO || nuevoEstado == CANCELADO;
            case ENVIADO -> 
                nuevoEstado == ENTREGADO;
            default -> false;
        };
    }
}
