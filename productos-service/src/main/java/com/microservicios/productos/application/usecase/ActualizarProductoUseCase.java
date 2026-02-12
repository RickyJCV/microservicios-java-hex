package com.microservicios.productos.application.usecase;

import com.microservicios.productos.domain.model.Producto;
import com.microservicios.productos.domain.port.ProductoEventPublisher;
import com.microservicios.productos.domain.port.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Caso de uso para actualizar productos existentes.
 * Demuestra el manejo de actualización con validación previa y eventos.
 */
@Service
public class ActualizarProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActualizarProductoUseCase.class);

    private final ProductoRepository productoRepository;
    private final ProductoEventPublisher eventPublisher;

    public ActualizarProductoUseCase(ProductoRepository productoRepository, ProductoEventPublisher eventPublisher) {
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Actualiza un producto existente.
     * 
     * Flujo:
     * 1. Verifica que el producto existe
     * 2. Crea una nueva instancia con los datos actualizados (inmutabilidad)
     * 3. Guarda en el repositorio
     * 4. Publica evento de actualización
     *
     * @param id ID del producto a actualizar
     * @param nombre Nuevo nombre
     * @param descripcion Nueva descripción
     * @param precio Nuevo precio
     * @param stock Nuevo stock
     * @param categoria Nueva categoría
     * @param activo Nuevo estado activo
     * @return Mono que emite el producto actualizado
     */
    public Mono<Producto> ejecutar(String id, String nombre, String descripcion, 
                                   BigDecimal precio, Integer stock, String categoria, 
                                   Boolean activo) {
        
        log.info("Actualizando producto con ID: {}", id);
        
        return productoRepository.buscarPorId(id)
                // switchIfEmpty maneja el caso de producto no encontrado
                .switchIfEmpty(Mono.error(
                    new BuscarProductoUseCase.ProductoNoEncontradoException(
                        "No se puede actualizar. Producto no encontrado con ID: " + id)
                ))
                // map transforma el producto actual en uno actualizado
                .map(productoActual -> {
                    // Detectar cambios de stock para publicar evento específico
                    Integer stockAnterior = productoActual.stock();
                    boolean cambioStock = !stock.equals(stockAnterior);
                    
                    // Crear nueva instancia con datos actualizados (Records son inmutables)
                    Producto productoActualizado = new Producto(
                            id,
                            nombre,
                            descripcion,
                            precio,
                            stock,
                            categoria,
                            activo,
                            productoActual.fechaCreacion(), // Mantener fecha de creación original
                            LocalDateTime.now() // Actualizar fecha de modificación
                    );
                    
                    return new ActualizacionInfo(productoActualizado, cambioStock, stockAnterior);
                })
                // flatMap para operaciones asíncronas encadenadas
                .flatMap(info -> 
                    productoRepository.guardar(info.producto())
                            .flatMap(productoGuardado -> {
                                // Si cambió el stock, publicar evento específico
                                Mono<Void> eventoStock = info.cambioStock() 
                                    ? eventPublisher.publicarCambioStock(
                                        productoGuardado.id(), 
                                        info.stockAnterior(), 
                                        productoGuardado.stock()
                                      )
                                    : Mono.empty();
                                
                                // Publicar evento general de actualización y evento de stock
                                return Mono.when(
                                    eventPublisher.publicarProductoActualizado(productoGuardado),
                                    eventoStock
                                ).thenReturn(productoGuardado);
                            })
                )
                .doOnSuccess(producto -> 
                    log.info("Producto actualizado exitosamente: {}", producto.id())
                )
                .doOnError(error -> 
                    log.error("Error al actualizar producto {}: {}", id, error.getMessage())
                );
    }

    /**
     * Actualiza solo el stock de un producto.
     * Método especializado que usa el método del dominio.
     *
     * @param id ID del producto
     * @param nuevoStock Nuevo stock
     * @return Mono que emite el producto actualizado
     */
    public Mono<Producto> actualizarStock(String id, Integer nuevoStock) {
        log.info("Actualizando stock del producto {} a {}", id, nuevoStock);
        
        return productoRepository.buscarPorId(id)
                .switchIfEmpty(Mono.error(
                    new BuscarProductoUseCase.ProductoNoEncontradoException(
                        "Producto no encontrado con ID: " + id)
                ))
                // Usar el método del dominio para actualizar stock
                .map(producto -> producto.actualizarStock(nuevoStock))
                .flatMap(producto -> 
                    productoRepository.guardar(producto)
                            .flatMap(productoGuardado ->
                                eventPublisher.publicarCambioStock(
                                    id, 
                                    producto.stock(), 
                                    nuevoStock
                                ).thenReturn(productoGuardado)
                            )
                )
                .doOnSuccess(p -> log.info("Stock actualizado para producto: {}", id));
    }

    /**
     * Record interno para transportar información de actualización.
     * Demuestra el uso de Records para DTOs internos.
     */
    private record ActualizacionInfo(
        Producto producto,
        boolean cambioStock,
        Integer stockAnterior
    ) {}
}
