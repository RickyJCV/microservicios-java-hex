package com.microservicios.productos.domain.port;

import com.microservicios.productos.domain.model.Producto;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para publicar eventos de productos.
 * Este puerto permite la comunicación asíncrona entre microservicios mediante mensajería (RabbitMQ).
 * 
 * Siguiendo el patrón Event-Driven Architecture, cuando ocurre un cambio en un producto,
 * se publica un evento para que otros microservicios puedan reaccionar.
 */
public interface ProductoEventPublisher {

    /**
     * Publica un evento cuando se crea un nuevo producto.
     * Otros microservicios pueden suscribirse a este evento para actualizar sus propios datos.
     *
     * @param producto Producto creado
     * @return Mono<Void> que completa cuando el evento se publica exitosamente
     */
    Mono<Void> publicarProductoCreado(Producto producto);

    /**
     * Publica un evento cuando se actualiza un producto.
     * Útil para mantener la consistencia eventual entre microservicios.
     *
     * @param producto Producto actualizado
     * @return Mono<Void> que completa cuando el evento se publica exitosamente
     */
    Mono<Void> publicarProductoActualizado(Producto producto);

    /**
     * Publica un evento cuando se elimina un producto.
     * Permite a otros servicios limpiar referencias o tomar acciones apropiadas.
     *
     * @param productoId ID del producto eliminado
     * @return Mono<Void> que completa cuando el evento se publica exitosamente
     */
    Mono<Void> publicarProductoEliminado(String productoId);

    /**
     * Publica un evento cuando cambia el stock de un producto.
     * Importante para servicios que necesitan conocer disponibilidad en tiempo real.
     *
     * @param productoId ID del producto
     * @param stockAnterior Stock anterior
     * @param stockNuevo Stock nuevo
     * @return Mono<Void> que completa cuando el evento se publica exitosamente
     */
    Mono<Void> publicarCambioStock(String productoId, Integer stockAnterior, Integer stockNuevo);
}
