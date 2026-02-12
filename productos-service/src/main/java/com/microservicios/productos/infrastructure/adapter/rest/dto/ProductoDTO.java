package com.microservicios.productos.infrastructure.adapter.rest.dto;

import com.microservicios.productos.domain.model.Producto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs (Data Transfer Objects) para las peticiones y respuestas REST.
 * Estos objetos son la interfaz entre el cliente HTTP y la aplicación.
 * 
 * Usamos Records de Java para DTOs inmutables y concisos.
 * Las anotaciones de validación (@NotNull, @NotBlank, etc.) provienen de Jakarta Validation.
 * Las anotaciones @Schema son de OpenAPI/Swagger para documentación.
 */
public class ProductoDTO {

    /**
     * DTO para crear un nuevo producto.
     * Solo incluye los campos necesarios para la creación.
     */
    @Schema(description = "Datos para crear un nuevo producto")
    public record CrearProductoRequest(
            
            @Schema(description = "Nombre del producto", example = "Laptop Dell XPS 15")
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
            String nombre,
            
            @Schema(description = "Descripción detallada del producto", 
                    example = "Laptop de alto rendimiento con procesador Intel i7")
            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
            String descripcion,
            
            @Schema(description = "Precio del producto", example = "1299.99")
            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
            @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
            BigDecimal precio,
            
            @Schema(description = "Cantidad en inventario", example = "50")
            @NotNull(message = "El stock es obligatorio")
            @Min(value = 0, message = "El stock no puede ser negativo")
            Integer stock,
            
            @Schema(description = "Categoría del producto", example = "Electrónica")
            @NotBlank(message = "La categoría es obligatoria")
            @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
            String categoria
    ) {
        /**
         * Convierte el DTO a modelo de dominio.
         * Usa el método de fábrica del dominio.
         */
        public Producto toDomain() {
            return Producto.crear(nombre, descripcion, precio, stock, categoria);
        }
    }

    /**
     * DTO para actualizar un producto existente.
     * Incluye todos los campos modificables.
     */
    @Schema(description = "Datos para actualizar un producto existente")
    public record ActualizarProductoRequest(
            
            @Schema(description = "Nombre del producto", example = "Laptop Dell XPS 15 (2024)")
            @NotBlank(message = "El nombre es obligatorio")
            @Size(min = 3, max = 100)
            String nombre,
            
            @Schema(description = "Descripción del producto")
            @NotBlank(message = "La descripción es obligatoria")
            @Size(max = 500)
            String descripcion,
            
            @Schema(description = "Precio actualizado", example = "1199.99")
            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "0.0", inclusive = false)
            @Digits(integer = 10, fraction = 2)
            BigDecimal precio,
            
            @Schema(description = "Stock actualizado", example = "45")
            @NotNull(message = "El stock es obligatorio")
            @Min(value = 0)
            Integer stock,
            
            @Schema(description = "Categoría del producto", example = "Electrónica")
            @NotBlank(message = "La categoría es obligatoria")
            @Size(max = 50)
            String categoria,
            
            @Schema(description = "Indica si el producto está activo", example = "true")
            @NotNull(message = "El campo activo es obligatorio")
            Boolean activo
    ) {}

    /**
     * DTO para actualizar solo el stock.
     * Útil para operaciones de inventario rápidas.
     */
    @Schema(description = "Actualización de stock del producto")
    public record ActualizarStockRequest(
            
            @Schema(description = "Nuevo stock", example = "100")
            @NotNull(message = "El stock es obligatorio")
            @Min(value = 0, message = "El stock no puede ser negativo")
            Integer stock
    ) {}

    /**
     * DTO de respuesta que representa un producto.
     * Incluye todos los campos, incluyendo los generados (id, fechas).
     */
    @Schema(description = "Respuesta con los datos completos de un producto")
    public record ProductoResponse(
            
            @Schema(description = "ID único del producto", example = "123e4567-e89b-12d3-a456-426614174000")
            String id,
            
            @Schema(description = "Nombre del producto")
            String nombre,
            
            @Schema(description = "Descripción del producto")
            String descripcion,
            
            @Schema(description = "Precio del producto")
            BigDecimal precio,
            
            @Schema(description = "Stock disponible")
            Integer stock,
            
            @Schema(description = "Categoría del producto")
            String categoria,
            
            @Schema(description = "Estado del producto (activo/inactivo)")
            Boolean activo,
            
            @Schema(description = "Fecha de creación")
            LocalDateTime fechaCreacion,
            
            @Schema(description = "Fecha de última actualización")
            LocalDateTime fechaActualizacion
    ) {
        /**
         * Crea un ProductoResponse desde el modelo de dominio.
         * Método estático de fábrica.
         */
        public static ProductoResponse fromDomain(Producto producto) {
            return new ProductoResponse(
                    producto.id(),
                    producto.nombre(),
                    producto.descripcion(),
                    producto.precio(),
                    producto.stock(),
                    producto.categoria(),
                    producto.activo(),
                    producto.fechaCreacion(),
                    producto.fechaActualizacion()
            );
        }
    }
}
