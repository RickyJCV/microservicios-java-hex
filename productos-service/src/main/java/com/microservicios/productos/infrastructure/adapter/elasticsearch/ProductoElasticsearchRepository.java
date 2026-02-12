package com.microservicios.productos.infrastructure.adapter.elasticsearch;

import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repositorio de Spring Data Elasticsearch para ProductoDocument.
 * 
 * ReactiveElasticsearchRepository proporciona operaciones CRUD reactivas automáticamente:
 * - save(): Guarda o actualiza un documento
 * - findById(): Busca por ID
 * - findAll(): Obtiene todos los documentos
 * - deleteById(): Elimina por ID
 * - count(): Cuenta documentos
 * 
 * Además, Spring Data genera implementaciones automáticas basadas en los nombres de métodos.
 * Ejemplos:
 * - findByNombre → busca por campo nombre exacto
 * - findByNombreContaining → búsqueda parcial en nombre
 * - findByCategoria → busca por categoría exacta
 * - findByActivoTrue → busca productos activos
 */
@Repository
public interface ProductoElasticsearchRepository 
        extends ReactiveElasticsearchRepository<ProductoDocument, String> {

    /**
     * Busca productos por categoría.
     * Spring Data genera automáticamente la query basándose en el nombre del método.
     *
     * @param categoria Categoría a buscar
     * @return Flux de productos en la categoría
     */
    Flux<ProductoDocument> findByCategoria(String categoria);

    /**
     * Busca productos cuyo nombre contenga el texto especificado.
     * Usa "Containing" para búsqueda parcial (LIKE en SQL).
     * 
     * Elasticsearch ejecutará una búsqueda de texto completo.
     *
     * @param nombre Texto a buscar en el nombre
     * @return Flux de productos que coinciden
     */
    Flux<ProductoDocument> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca productos activos.
     * Ejemplo de query method con valor booleano.
     *
     * @return Flux de productos activos
     */
    Flux<ProductoDocument> findByActivoTrue();

    /**
     * Busca productos por categoría que estén activos.
     * Ejemplo de método con múltiples condiciones (AND implícito).
     *
     * @param categoria Categoría a buscar
     * @return Flux de productos activos en la categoría
     */
    Flux<ProductoDocument> findByCategoriaAndActivoTrue(String categoria);

    /**
     * Busca productos con stock mayor o igual al especificado.
     * Ejemplo de comparación con "GreaterThanEqual".
     *
     * @param stock Stock mínimo
     * @return Flux de productos con stock suficiente
     */
    Flux<ProductoDocument> findByStockGreaterThanEqual(Integer stock);
}
