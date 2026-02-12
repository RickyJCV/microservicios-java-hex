package com.microservicios.productos.infrastructure.adapter.elasticsearch;

import com.microservicios.productos.domain.model.Producto;
import com.microservicios.productos.domain.port.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adaptador que implementa el puerto ProductoRepository.
 * Esta clase pertenece a la capa de infraestructura y conecta el dominio con Elasticsearch.
 * 
 * En arquitectura hexagonal:
 * - El dominio define PUERTOS (interfaces)
 * - La infraestructura implementa ADAPTADORES (implementaciones concretas)
 * - El dominio NO conoce a Elasticsearch, solo al puerto
 * 
 * @Component marca la clase como un componente de Spring para inyección de dependencias
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductoRepositoryAdapter implements ProductoRepository {

    // Repositorio de Spring Data Elasticsearch inyectado por constructor (Lombok)
    private final ProductoElasticsearchRepository elasticsearchRepository;

    /**
     * Guarda un producto en Elasticsearch.
     * Genera un ID si no existe.
     * 
     * Demuestra:
     * - Mapeo entre dominio e infraestructura
     * - Uso de map() para transformación
     */
    @Override
    public Mono<Producto> guardar(Producto producto) {
        log.debug("Guardando producto en Elasticsearch: {}", producto.nombre());
        
        // Generar ID si es nuevo producto
        String id = producto.id() != null ? producto.id() : UUID.randomUUID().toString();
        
        // Crear nuevo producto con ID
        Producto productoConId = new Producto(
                id,
                producto.nombre(),
                producto.descripcion(),
                producto.precio(),
                producto.stock(),
                producto.categoria(),
                producto.activo(),
                producto.fechaCreacion(),
                producto.fechaActualizacion()
        );
        
        // Convertir a documento, guardar y convertir de vuelta a dominio
        return Mono.just(productoConId)
                .map(ProductoDocument::fromDomain) // Convertir dominio → documento
                .flatMap(elasticsearchRepository::save) // Guardar en Elasticsearch
                .map(ProductoDocument::toDomain) // Convertir documento → dominio
                .doOnSuccess(p -> log.debug("Producto guardado con ID: {}", p.id()));
    }

    /**
     * Busca un producto por ID.
     * 
     * Demuestra:
     * - Uso de flatMap para operaciones que retornan Mono
     * - Mapeo de Optional a dominio
     */
    @Override
    public Mono<Producto> buscarPorId(String id) {
        log.debug("Buscando producto por ID: {}", id);
        
        return elasticsearchRepository.findById(id)
                .map(ProductoDocument::toDomain)
                .doOnSuccess(p -> {
                    if (p != null) {
                        log.debug("Producto encontrado: {}", p.nombre());
                    } else {
                        log.debug("Producto no encontrado con ID: {}", id);
                    }
                });
    }

    /**
     * Obtiene todos los productos.
     * 
     * Demuestra:
     * - Uso de Flux para colecciones
     * - Transformación de stream con map()
     */
    @Override
    public Flux<Producto> buscarTodos() {
        log.debug("Buscando todos los productos");
        
        return elasticsearchRepository.findAll()
                // map transforma cada ProductoDocument en Producto
                .map(ProductoDocument::toDomain) // Referencia a método
                .doOnComplete(() -> log.debug("Búsqueda completada"));
    }

    /**
     * Busca productos por categoría.
     * 
     * Demuestra:
     * - Uso de query methods de Spring Data
     * - Transformación de Flux
     */
    @Override
    public Flux<Producto> buscarPorCategoria(String categoria) {
        log.debug("Buscando productos por categoría: {}", categoria);
        
        return elasticsearchRepository.findByCategoriaAndActivoTrue(categoria)
                .map(ProductoDocument::toDomain)
                .doOnNext(p -> log.debug("Producto encontrado: {}", p.nombre()));
    }

    /**
     * Busca productos por nombre (búsqueda parcial).
     * Aprovecha las capacidades de búsqueda de texto completo de Elasticsearch.
     */
    @Override
    public Flux<Producto> buscarPorNombre(String nombre) {
        log.debug("Buscando productos por nombre: {}", nombre);
        
        return elasticsearchRepository.findByNombreContainingIgnoreCase(nombre)
                .map(ProductoDocument::toDomain)
                .doOnNext(p -> log.debug("Producto encontrado: {}", p.nombre()));
    }

    /**
     * Elimina un producto por ID.
     */
    @Override
    public Mono<Void> eliminar(String id) {
        log.debug("Eliminando producto con ID: {}", id);
        
        return elasticsearchRepository.deleteById(id)
                .doOnSuccess(v -> log.debug("Producto eliminado: {}", id));
    }

    /**
     * Verifica si existe un producto.
     */
    @Override
    public Mono<Boolean> existePorId(String id) {
        log.debug("Verificando existencia de producto: {}", id);
        
        return elasticsearchRepository.existsById(id)
                .doOnNext(existe -> log.debug("Producto {} existe: {}", id, existe));
    }

    /**
     * Cuenta el total de productos.
     */
    @Override
    public Mono<Long> contar() {
        log.debug("Contando productos");
        
        return elasticsearchRepository.count()
                .doOnNext(count -> log.debug("Total de productos: {}", count));
    }
}
