package com.microservicios.productos.application.usecase;

import com.microservicios.productos.domain.port.ProductoEventPublisher;
import com.microservicios.productos.domain.port.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para eliminar productos.
 * Implementa eliminación con verificación previa y publicación de eventos.
 */
@Service
public class EliminarProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(EliminarProductoUseCase.class);

    private final ProductoRepository productoRepository;
    private final ProductoEventPublisher eventPublisher;

    public EliminarProductoUseCase(ProductoRepository productoRepository, ProductoEventPublisher eventPublisher) {
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Elimina un producto por su ID.
     * 
     * Flujo:
     * 1. Verifica que el producto existe
     * 2. Elimina del repositorio
     * 3. Publica evento de eliminación
     * 
     * @param id ID del producto a eliminar
     * @return Mono<Void> que completa cuando la eliminación finaliza
     */
    public Mono<Void> ejecutar(String id) {
        log.info("Iniciando eliminación de producto con ID: {}", id);
        
        return productoRepository.existePorId(id)
                // flatMap convierte el Boolean en un flujo reactivo
                .flatMap(existe -> {
                    if (!existe) {
                        // Si no existe, retornar un error
                        return Mono.error(
                            new BuscarProductoUseCase.ProductoNoEncontradoException(
                                "No se puede eliminar. Producto no encontrado con ID: " + id
                            )
                        );
                    }
                    
                    // Si existe, proceder con la eliminación
                    return productoRepository.eliminar(id)
                            // then ejecuta otra operación después de completar la anterior
                            .then(eventPublisher.publicarProductoEliminado(id));
                })
                .doOnSuccess(v -> 
                    log.info("Producto eliminado exitosamente: {}", id)
                )
                .doOnError(error -> 
                    log.error("Error al eliminar producto {}: {}", id, error.getMessage())
                );
    }

    /**
     * Elimina múltiples productos en lote.
     * Demuestra el uso de Flux para operaciones en batch.
     *
     * @param ids Lista de IDs de productos a eliminar
     * @return Mono<Void> que completa cuando todas las eliminaciones finalizan
     */
    public Mono<Void> eliminarVarios(java.util.List<String> ids) {
        log.info("Iniciando eliminación en lote de {} productos", ids.size());
        
        // fromIterable convierte una colección en un Flux
        return reactor.core.publisher.Flux.fromIterable(ids)
                // flatMap procesa cada ID de forma concurrente
                .flatMap(this::ejecutar)
                // collectList espera a que todos completen
                .collectList()
                // then descarta el resultado y completa
                .then()
                .doOnSuccess(v -> 
                    log.info("Eliminación en lote completada: {} productos", ids.size())
                );
    }

    /**
     * Elimina productos inactivos antiguos (soft delete).
     * Útil para tareas de limpieza programadas.
     *
     * @param diasInactivos Número de días de inactividad
     * @return Mono<Long> que emite el número de productos eliminados
     */
    public Mono<Long> eliminarInactivos(int diasInactivos) {
        log.info("Eliminando productos inactivos de más de {} días", diasInactivos);
        
        java.time.LocalDateTime fechaLimite = 
            java.time.LocalDateTime.now().minusDays(diasInactivos);
        
        return productoRepository.buscarTodos()
                // filter con condición compuesta usando &&
                .filter(producto -> 
                    !producto.activo() && 
                    producto.fechaActualizacion().isBefore(fechaLimite)
                )
                // flatMap para eliminar cada producto
                .flatMap(producto -> 
                    productoRepository.eliminar(producto.id())
                            .thenReturn(1L) // Retorna 1 por cada eliminación exitosa
                )
                // reduce suma todos los 1s para contar eliminaciones
                .reduce(0L, Long::sum) // Referencia a método para suma
                .doOnSuccess(count -> 
                    log.info("Productos inactivos eliminados: {}", count)
                );
    }
}
