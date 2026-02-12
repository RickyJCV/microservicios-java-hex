package com.microservicios.productos.application.usecase;

import com.microservicios.productos.domain.model.Producto;
import com.microservicios.productos.domain.port.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para buscar productos.
 * Centraliza toda la lógica de búsqueda y consulta de productos.
 */
@Service
public class BuscarProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(BuscarProductoUseCase.class);

    private final ProductoRepository productoRepository;

    public BuscarProductoUseCase(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Busca un producto por su ID.
     *
     * @param id Identificador del producto
     * @return Mono que emite el producto encontrado o error si no existe
     */
    public Mono<Producto> ejecutarPorId(String id) {
        log.info("Buscando producto con ID: {}", id);
        
        return productoRepository.buscarPorId(id)
                .doOnSuccess(producto -> {
                    if (producto != null) {
                        log.info("Producto encontrado: {}", producto.nombre());
                    } else {
                        log.warn("Producto no encontrado con ID: {}", id);
                    }
                })
                // switchIfEmpty permite manejar el caso cuando no se encuentra el producto
                .switchIfEmpty(Mono.error(
                    new ProductoNoEncontradoException("Producto no encontrado con ID: " + id)
                ));
    }

    /**
     * Obtiene todos los productos.
     * Usa collectList() para convertir Flux en Mono<List> si es necesario.
     *
     * @return Flux que emite todos los productos
     */
    public Flux<Producto> ejecutarTodos() {
        log.info("Consultando todos los productos");
        
        return productoRepository.buscarTodos()
                .doOnComplete(() -> log.info("Consulta de todos los productos completada"))
                .doOnError(error -> log.error("Error al consultar productos: {}", error.getMessage()));
    }

    /**
     * Busca productos por categoría.
     * Demuestra el uso de filter() en streams reactivos.
     *
     * @param categoria Categoría a buscar
     * @return Flux que emite los productos de la categoría
     */
    public Flux<Producto> ejecutarPorCategoria(String categoria) {
        log.info("Buscando productos de la categoría: {}", categoria);
        
        return productoRepository.buscarPorCategoria(categoria)
                // filter permite filtrar elementos basándose en una condición
                .filter(Producto::activo) // Solo productos activos (referencia a método)
                .doOnNext(producto -> 
                    // doOnNext se ejecuta para cada elemento emitido
                    log.debug("Producto encontrado en categoría {}: {}", categoria, producto.nombre())
                );
    }

    /**
     * Busca productos por nombre (búsqueda parcial).
     * Utiliza las capacidades de búsqueda de texto de Elasticsearch.
     *
     * @param nombre Texto a buscar
     * @return Flux que emite los productos que coinciden
     */
    public Flux<Producto> ejecutarPorNombre(String nombre) {
        log.info("Buscando productos con nombre: {}", nombre);
        
        return productoRepository.buscarPorNombre(nombre)
                .filter(Producto::activo)
                // sort ordena los elementos usando un Comparator
                .sort((p1, p2) -> p1.nombre().compareToIgnoreCase(p2.nombre()))
                .doOnComplete(() -> log.info("Búsqueda por nombre completada"));
    }

    /**
     * Busca productos con stock disponible.
     * Ejemplo de uso de filter con expresión lambda.
     *
     * @param stockMinimo Stock mínimo requerido
     * @return Flux que emite productos con stock suficiente
     */
    public Flux<Producto> ejecutarConStock(Integer stockMinimo) {
        log.info("Buscando productos con stock mínimo de: {}", stockMinimo);
        
        return productoRepository.buscarTodos()
                // Lambda que define la condición de filtrado
                .filter(producto -> producto.stock() >= stockMinimo && producto.activo())
                .doOnNext(producto -> 
                    log.debug("Producto con stock disponible: {} (Stock: {})", 
                        producto.nombre(), producto.stock())
                );
    }

    /**
     * Excepción personalizada para producto no encontrado.
     */
    public static class ProductoNoEncontradoException extends RuntimeException {
        public ProductoNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }
}
