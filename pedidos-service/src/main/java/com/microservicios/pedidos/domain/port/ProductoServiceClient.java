package com.microservicios.pedidos.domain.port;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Puerto de salida para comunicarse con el microservicio de productos.
 * 
 * Este puerto define el contrato para obtener información de productos
 * desde el microservicio externo. Permite mantener el dominio desacoplado
 * de la implementación específica de comunicación HTTP.
 * 
 * Será implementado por un adaptador que usa WebClient para hacer
 * peticiones HTTP al microservicio de productos.
 */
public interface ProductoServiceClient {

    /**
     * Obtiene la información de un producto por su ID.
     * Realiza una llamada HTTP al microservicio de productos.
     *
     * @param productoId ID del producto a consultar
     * @return Mono que emite la información del producto
     */
    Mono<ProductoInfo> obtenerProducto(String productoId);

    /**
     * Verifica si un producto existe y tiene stock disponible.
     * Útil para validar pedidos antes de confirmarlos.
     *
     * @param productoId ID del producto
     * @param cantidadRequerida Cantidad que se desea pedir
     * @return Mono que emite true si hay stock suficiente
     */
    Mono<Boolean> verificarDisponibilidad(String productoId, Integer cantidadRequerida);

    /**
     * Actualiza el stock de un producto.
     * Se llama cuando un pedido es confirmado para decrementar el stock.
     *
     * @param productoId ID del producto
     * @param nuevoStock Nuevo stock del producto
     * @return Mono<Void> que completa cuando se actualiza
     */
    Mono<Void> actualizarStock(String productoId, Integer nuevoStock);

    /**
     * Record que representa la información básica de un producto.
     * Solo contiene los campos necesarios para el contexto de pedidos.
     * 
     * Este DTO es específico del dominio de pedidos y no depende
     * de la estructura exacta del microservicio de productos.
     */
    record ProductoInfo(
            String id,
            String nombre,
            BigDecimal precio,
            Integer stock,
            Boolean activo
    ) {
        /**
         * Verifica si el producto tiene stock disponible.
         *
         * @param cantidad Cantidad requerida
         * @return true si hay stock suficiente
         */
        public boolean tieneStock(Integer cantidad) {
            return activo && stock != null && stock >= cantidad;
        }
    }
}
