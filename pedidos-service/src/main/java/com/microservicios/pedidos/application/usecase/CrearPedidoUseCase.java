package com.microservicios.pedidos.application.usecase;

import com.microservicios.pedidos.domain.model.ItemPedido;
import com.microservicios.pedidos.domain.model.Pedido;
import com.microservicios.pedidos.domain.port.PedidoEventPublisher;
import com.microservicios.pedidos.domain.port.PedidoRepository;
import com.microservicios.pedidos.domain.port.ProductoServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Caso de uso para crear un nuevo pedido.
 * 
 * Este caso de uso demuestra:
 * - Coordinación entre múltiples microservicios
 * - Validación de stock antes de crear el pedido
 * - Uso de programación reactiva para llamadas paralelas
 * - Manejo de transacciones distribuidas
 */
@Service
public class CrearPedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearPedidoUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;
    private final ProductoServiceClient productoServiceClient;

    public CrearPedidoUseCase(PedidoRepository pedidoRepository, PedidoEventPublisher eventPublisher, ProductoServiceClient productoServiceClient) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
        this.productoServiceClient = productoServiceClient;
    }

    /**
     * Ejecuta el caso de uso de creación de pedido.
     * 
     * Flujo:
     * 1. Verificar que todos los productos existen
     * 2. Verificar que hay stock disponible de cada producto
     * 3. Crear el pedido
     * 4. Actualizar el stock de los productos
     * 5. Publicar evento de pedido creado
     * 
     * Demuestra:
     * - flatMap para encadenar operaciones asíncronas
     * - Flux.fromIterable para procesar listas reactivamente
     * - collectList() para convertir Flux a Mono<List>
     * - zip() para combinar múltiples Monos
     *
     * @param clienteId ID del cliente
     * @param clienteNombre Nombre del cliente
     * @param itemsRequest Lista de items solicitados
     * @return Mono que emite el pedido creado
     */
    public Mono<Pedido> ejecutar(String clienteId, String clienteNombre, 
                                 List<ItemPedidoRequest> itemsRequest) {
        
        log.info("Creando pedido para cliente: {}", clienteId);
        
        // Paso 1 y 2: Verificar productos y stock en paralelo
        return verificarYEnriquecerItems(itemsRequest)
                // Paso 3: Crear el pedido con la información validada
                .flatMap(items -> {
                    Pedido nuevoPedido = Pedido.crear(clienteId, clienteNombre, items);
                    return pedidoRepository.guardar(nuevoPedido);
                })
                // Paso 4: Actualizar stock de productos
                .flatMap(pedidoGuardado -> 
                    actualizarStockProductos(pedidoGuardado.items())
                            .thenReturn(pedidoGuardado)
                )
                // Paso 5: Publicar evento
                .flatMap(pedidoGuardado ->
                    eventPublisher.publicarPedidoCreado(pedidoGuardado)
                            .thenReturn(pedidoGuardado)
                )
                .doOnSuccess(pedido -> 
                    log.info("Pedido creado exitosamente con ID: {}", pedido.id())
                )
                .doOnError(error ->
                    log.error("Error al crear pedido para cliente {}: {}", 
                        clienteId, error.getMessage(), error)
                );
    }

    /**
     * Verifica que los productos existen y tienen stock, luego crea los ItemPedido.
     * 
     * Demuestra:
     * - Flux.fromIterable() para convertir lista a flujo reactivo
     * - flatMap() con Flux para procesar items en paralelo
     * - collectList() para agrupar resultados
     * 
     * @param itemsRequest Lista de items solicitados
     * @return Mono que emite lista de ItemPedido validados
     */
    private Mono<List<ItemPedido>> verificarYEnriquecerItems(List<ItemPedidoRequest> itemsRequest) {
        
        // Convertir lista a Flux para procesamiento reactivo
        return Flux.fromIterable(itemsRequest)
                // flatMap procesa cada item de forma asíncrona y en paralelo
                .flatMap(itemRequest -> 
                    // Obtener información del producto del microservicio
                    productoServiceClient.obtenerProducto(itemRequest.productoId())
                            .flatMap(productoInfo -> {
                                // Verificar que hay stock disponible
                                if (!productoInfo.tieneStock(itemRequest.cantidad())) {
                                    return Mono.error(new StockInsuficienteException(
                                        String.format("Stock insuficiente para producto %s. " +
                                            "Disponible: %d, Solicitado: %d",
                                            productoInfo.nombre(),
                                            productoInfo.stock(),
                                            itemRequest.cantidad())
                                    ));
                                }
                                
                                // Crear ItemPedido con la información del producto
                                ItemPedido item = ItemPedido.crear(
                                        productoInfo.id(),
                                        productoInfo.nombre(),
                                        productoInfo.precio(),
                                        itemRequest.cantidad()
                                );
                                
                                return Mono.just(item);
                            })
                            .onErrorMap(error -> {
                                // Si el producto no existe, transformar el error
                                if (error instanceof StockInsuficienteException) {
                                    return error; // Propagar error de stock
                                }
                                return new ProductoNoEncontradoException(
                                    "Producto no encontrado: " + itemRequest.productoId()
                                );
                            })
                )
                // Agrupar todos los items en una lista
                .collectList()
                .doOnNext(items -> 
                    log.debug("Items verificados y enriquecidos: {} productos", items.size())
                );
    }

    /**
     * Actualiza el stock de todos los productos en el pedido.
     * 
     * Demuestra:
     * - flatMap con Flux para operaciones paralelas
     * - then() para esperar a que completen todas las operaciones
     * 
     * @param items Items del pedido
     * @return Mono<Void> que completa cuando se actualizan todos
     */
    private Mono<Void> actualizarStockProductos(List<ItemPedido> items) {
        return Flux.fromIterable(items)
                .flatMap(item ->
                    // Obtener producto actual para calcular nuevo stock
                    productoServiceClient.obtenerProducto(item.productoId())
                            .flatMap(productoInfo -> {
                                // Calcular nuevo stock
                                int nuevoStock = productoInfo.stock() - item.cantidad();
                                
                                // Actualizar stock en el microservicio de productos
                                return productoServiceClient.actualizarStock(
                                        item.productoId(), 
                                        nuevoStock
                                );
                            })
                )
                // Esperar a que todas las actualizaciones completen
                .then()
                .doOnSuccess(v -> log.debug("Stock actualizado para {} productos", items.size()));
    }

    /**
     * Record para representar un item en la petición de creación.
     * Solo contiene los datos mínimos que el cliente proporciona.
     */
    public record ItemPedidoRequest(
            String productoId,
            Integer cantidad
    ) {}

    /**
     * Excepción cuando un producto no se encuentra.
     */
    public static class ProductoNoEncontradoException extends RuntimeException {
        public ProductoNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }

    /**
     * Excepción cuando no hay stock suficiente.
     */
    public static class StockInsuficienteException extends RuntimeException {
        public StockInsuficienteException(String mensaje) {
            super(mensaje);
        }
    }
}
