package com.microservicios.productos.domain.port;

import com.microservicios.productos.domain.model.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (Output Port) que define el contrato para el repositorio de productos.
 * Este puerto es parte de la capa de dominio y será implementado por un adaptador en la capa de infraestructura.
 * 
 * En arquitectura hexagonal:
 * - Los puertos son interfaces que definen los contratos
 * - Los adaptadores son las implementaciones concretas
 * - El dominio depende de puertos, no de implementaciones
 * 
 * Usamos tipos reactivos (Mono y Flux) de Project Reactor para soportar WebFlux.
 */
public interface ProductoRepository {

    /**
     * Guarda un producto en el repositorio.
     * Usa Mono<Producto> que representa 0 o 1 elemento de forma asíncrona.
     *
     * @param producto Producto a guardar
     * @return Mono que emite el producto guardado con su ID generado
     */
    Mono<Producto> guardar(Producto producto);

    /**
     * Busca un producto por su ID.
     * 
     * @param id Identificador del producto
     * @return Mono que emite el producto encontrado o Mono.empty() si no existe
     */
    Mono<Producto> buscarPorId(String id);

    /**
     * Obtiene todos los productos.
     * Usa Flux<Producto> que representa 0 a N elementos de forma asíncrona.
     *
     * @return Flux que emite todos los productos
     */
    Flux<Producto> buscarTodos();

    /**
     * Busca productos por categoría.
     * Ejemplo de uso de Streams reactivos para filtrado.
     *
     * @param categoria Categoría a buscar
     * @return Flux que emite los productos de la categoría especificada
     */
    Flux<Producto> buscarPorCategoria(String categoria);

    /**
     * Busca productos por nombre (búsqueda parcial).
     * Útil para funcionalidad de búsqueda con Elasticsearch.
     *
     * @param nombre Texto a buscar en el nombre del producto
     * @return Flux que emite los productos que coinciden con la búsqueda
     */
    Flux<Producto> buscarPorNombre(String nombre);

    /**
     * Elimina un producto por su ID.
     *
     * @param id Identificador del producto a eliminar
     * @return Mono<Void> que completa cuando la eliminación finaliza
     */
    Mono<Void> eliminar(String id);

    /**
     * Verifica si existe un producto con el ID especificado.
     *
     * @param id Identificador del producto
     * @return Mono<Boolean> que emite true si existe, false si no
     */
    Mono<Boolean> existePorId(String id);

    /**
     * Cuenta el total de productos.
     *
     * @return Mono<Long> que emite el número total de productos
     */
    Mono<Long> contar();
}
