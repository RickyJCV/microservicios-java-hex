package com.microservicios.productos.infrastructure.adapter.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Documento de Elasticsearch que representa un Producto.
 * Esta clase es parte de la capa de infraestructura (adaptador de Elasticsearch).
 * 
 * Anotaciones de Lombok:
 * - @Data: genera getters, setters, toString, equals y hashCode
 * - @Builder: proporciona un patrón builder para crear instancias
 * - @NoArgsConstructor: genera constructor sin argumentos (requerido por Spring Data)
 * - @AllArgsConstructor: genera constructor con todos los argumentos
 * 
 * Anotaciones de Spring Data Elasticsearch:
 * - @Document: marca la clase como un documento de Elasticsearch
 * - @Id: marca el campo como identificador del documento
 * - @Field: configura el mapeo del campo en Elasticsearch
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "productos") // Define el índice de Elasticsearch
public class ProductoDocument {

    @Id
    private String id;

    /**
     * Campo de texto con análisis completo para búsqueda.
     * FieldType.Text permite búsqueda de texto completo.
     */
    @Field(type = FieldType.Text, analyzer = "spanish")
    private String nombre;

    @Field(type = FieldType.Text, analyzer = "spanish")
    private String descripcion;

    /**
     * Campo Double para precio.
     * Elasticsearch no soporta BigDecimal directamente.
     */
    @Field(type = FieldType.Double)
    private BigDecimal precio;

    @Field(type = FieldType.Integer)
    private Integer stock;

    /**
     * Campo Keyword para categoría.
     * FieldType.Keyword se usa para coincidencias exactas y agregaciones.
     */
    @Field(type = FieldType.Keyword)
    private String categoria;

    @Field(type = FieldType.Boolean)
    private Boolean activo;

    @Field(type = FieldType.Date)
    private LocalDateTime fechaCreacion;

    @Field(type = FieldType.Date)
    private LocalDateTime fechaActualizacion;

    /**
     * Convierte el documento de Elasticsearch a modelo de dominio.
     * Ejemplo de método de mapeo en la capa de infraestructura.
     *
     * @return Instancia de Producto del dominio
     */
    public com.microservicios.productos.domain.model.Producto toDomain() {
        return new com.microservicios.productos.domain.model.Producto(
                this.id,
                this.nombre,
                this.descripcion,
                this.precio,
                this.stock,
                this.categoria,
                this.activo,
                this.fechaCreacion,
                this.fechaActualizacion
        );
    }

    /**
     * Crea un documento de Elasticsearch desde el modelo de dominio.
     * Método estático de fábrica para conversión.
     *
     * @param producto Producto del dominio
     * @return ProductoDocument para Elasticsearch
     */
    public static ProductoDocument fromDomain(com.microservicios.productos.domain.model.Producto producto) {
        return ProductoDocument.builder()
                .id(producto.id())
                .nombre(producto.nombre())
                .descripcion(producto.descripcion())
                .precio(producto.precio())
                .stock(producto.stock())
                .categoria(producto.categoria())
                .activo(producto.activo())
                .fechaCreacion(producto.fechaCreacion())
                .fechaActualizacion(producto.fechaActualizacion())
                .build();
    }
}
