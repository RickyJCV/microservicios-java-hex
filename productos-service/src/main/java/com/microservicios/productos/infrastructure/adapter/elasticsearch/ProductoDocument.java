package com.microservicios.productos.infrastructure.adapter.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Documento de Elasticsearch que representa un Producto.
 * Esta clase es parte de la capa de infraestructura (adaptador de Elasticsearch).
 * 
 * Anotaciones de Spring Data Elasticsearch:
 * - @Document: marca la clase como un documento de Elasticsearch
 * - @Id: marca el campo como identificador del documento
 * - @Field: configura el mapeo del campo en Elasticsearch
 */
@Document(indexName = "productos")
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

    // Constructores
    public ProductoDocument() {
    }

    public ProductoDocument(String id, String nombre, String descripcion, BigDecimal precio,
                           Integer stock, String categoria, Boolean activo,
                           LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "ProductoDocument{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                ", categoria='" + categoria + '\'' +
                ", activo=" + activo +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductoDocument that = (ProductoDocument) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
        ProductoDocument doc = new ProductoDocument();
        doc.setId(producto.id());
        doc.setNombre(producto.nombre());
        doc.setDescripcion(producto.descripcion());
        doc.setPrecio(producto.precio());
        doc.setStock(producto.stock());
        doc.setCategoria(producto.categoria());
        doc.setActivo(producto.activo());
        doc.setFechaCreacion(producto.fechaCreacion());
        doc.setFechaActualizacion(producto.fechaActualizacion());
        return doc;
    }
}
