package com.microservicios.pedidos.domain.port;

import com.microservicios.pedidos.domain.model.EstadoPedido;
import com.microservicios.pedidos.domain.model.Pedido;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para el repositorio de pedidos.
 * Define las operaciones de persistencia para pedidos.
 */
public interface PedidoRepository {

    /**
     * Guarda un pedido en el repositorio.
     *
     * @param pedido Pedido a guardar
     * @return Mono que emite el pedido guardado con su ID
     */
    Mono<Pedido> guardar(Pedido pedido);

    /**
     * Busca un pedido por su ID.
     *
     * @param id Identificador del pedido
     * @return Mono que emite el pedido encontrado o Mono.empty()
     */
    Mono<Pedido> buscarPorId(String id);

    /**
     * Obtiene todos los pedidos.
     *
     * @return Flux que emite todos los pedidos
     */
    Flux<Pedido> buscarTodos();

    /**
     * Busca pedidos por ID de cliente.
     * Útil para mostrar el historial de pedidos de un cliente.
     *
     * @param clienteId ID del cliente
     * @return Flux que emite los pedidos del cliente
     */
    Flux<Pedido> buscarPorCliente(String clienteId);

    /**
     * Busca pedidos por estado.
     * Permite filtrar pedidos pendientes, confirmados, etc.
     *
     * @param estado Estado del pedido
     * @return Flux que emite los pedidos con el estado especificado
     */
    Flux<Pedido> buscarPorEstado(EstadoPedido estado);

    /**
     * Busca pedidos que contengan un producto específico.
     * Demuestra búsqueda en listas anidadas.
     *
     * @param productoId ID del producto
     * @return Flux que emite pedidos que contienen el producto
     */
    Flux<Pedido> buscarPorProducto(String productoId);

    /**
     * Elimina un pedido por su ID.
     *
     * @param id Identificador del pedido
     * @return Mono<Void> que completa cuando se elimina
     */
    Mono<Void> eliminar(String id);

    /**
     * Cuenta el total de pedidos.
     *
     * @return Mono que emite el número total de pedidos
     */
    Mono<Long> contar();

    /**
     * Cuenta pedidos por estado.
     * Útil para dashboards y estadísticas.
     *
     * @param estado Estado a contar
     * @return Mono que emite el número de pedidos en ese estado
     */
    Mono<Long> contarPorEstado(EstadoPedido estado);
}
