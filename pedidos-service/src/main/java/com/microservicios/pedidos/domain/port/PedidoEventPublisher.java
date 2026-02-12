package com.microservicios.pedidos.domain.port;

import com.microservicios.pedidos.domain.model.Pedido;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para publicar eventos de pedidos.
 * Permite la comunicación asíncrona con otros microservicios mediante eventos.
 */
public interface PedidoEventPublisher {

    /**
     * Publica un evento cuando se crea un nuevo pedido.
     *
     * @param pedido Pedido creado
     * @return Mono<Void> que completa cuando el evento se publica
     */
    Mono<Void> publicarPedidoCreado(Pedido pedido);

    /**
     * Publica un evento cuando se actualiza un pedido.
     *
     * @param pedido Pedido actualizado
     * @return Mono<Void> que completa cuando el evento se publica
     */
    Mono<Void> publicarPedidoActualizado(Pedido pedido);

    /**
     * Publica un evento cuando cambia el estado de un pedido.
     * Evento específico para cambios de estado (útil para notificaciones).
     *
     * @param pedidoId ID del pedido
     * @param estadoAnterior Estado anterior
     * @param estadoNuevo Estado nuevo
     * @return Mono<Void> que completa cuando el evento se publica
     */
    Mono<Void> publicarCambioEstado(String pedidoId, String estadoAnterior, String estadoNuevo);

    /**
     * Publica un evento cuando se cancela un pedido.
     *
     * @param pedidoId ID del pedido cancelado
     * @param motivo Motivo de la cancelación
     * @return Mono<Void> que completa cuando el evento se publica
     */
    Mono<Void> publicarPedidoCancelado(String pedidoId, String motivo);
}
