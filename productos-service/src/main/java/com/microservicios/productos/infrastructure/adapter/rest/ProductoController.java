package com.microservicios.productos.infrastructure.adapter.rest;

import com.microservicios.productos.application.usecase.*;
import com.microservicios.productos.infrastructure.adapter.rest.dto.ProductoDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para gestión de productos.
 * Expone los endpoints HTTP para las operaciones CRUD.
 * 
 * Este controlador es parte de la capa de infraestructura (adaptador REST).
 * Utiliza programación reactiva con WebFlux (Mono y Flux).
 * 
 * Anotaciones Spring:
 * - @RestController: Marca la clase como controlador REST
 * - @RequestMapping: Define el path base para todos los endpoints
 * - @RequiredArgsConstructor: Lombok genera constructor para inyección de dependencias
 * 
 * Anotaciones Swagger/OpenAPI:
 * - @Tag: Agrupa los endpoints en la documentación
 * - @Operation: Describe la operación del endpoint
 * - @ApiResponses: Define las posibles respuestas HTTP
 */
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "API para gestión de productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    // Inyección de casos de uso mediante constructor (generado por Lombok)
    private final CrearProductoUseCase crearProductoUseCase;
    private final BuscarProductoUseCase buscarProductoUseCase;
    private final ActualizarProductoUseCase actualizarProductoUseCase;
    private final EliminarProductoUseCase eliminarProductoUseCase;

    /**
     * Constructor para inyección de dependencias
     */
    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                            BuscarProductoUseCase buscarProductoUseCase,
                            ActualizarProductoUseCase actualizarProductoUseCase,
                            EliminarProductoUseCase eliminarProductoUseCase) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.buscarProductoUseCase = buscarProductoUseCase;
        this.actualizarProductoUseCase = actualizarProductoUseCase;
        this.eliminarProductoUseCase = eliminarProductoUseCase;
    }

    /**
     * Crea un nuevo producto.
     * 
     * POST /api/v1/productos
     * 
     * Demuestra:
     * - @PostMapping para HTTP POST
     * - @RequestBody con @Valid para validación automática
     * - @ResponseStatus para código HTTP 201 (Created)
     * - Uso de map() para transformar Producto a ProductoResponse
     */
    @Operation(
        summary = "Crear un nuevo producto",
        description = "Crea un nuevo producto en el sistema con la información proporcionada"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente",
            content = @Content(schema = @Schema(implementation = ProductoResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductoResponse> crearProducto(
            @Valid @RequestBody CrearProductoRequest request) {
        
        log.info("Solicitud para crear producto: {}", request.nombre());
        
        return crearProductoUseCase.ejecutar(
                request.nombre(),
                request.descripcion(),
                request.precio(),
                request.stock(),
                request.categoria()
        )
        // map transforma Producto en ProductoResponse
        .map(ProductoResponse::fromDomain)
        .doOnSuccess(response -> 
            log.info("Producto creado con ID: {}", response.id())
        );
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * GET /api/v1/productos/{id}
     * 
     * Demuestra:
     * - @GetMapping con @PathVariable
     * - Manejo de errores con onErrorResume
     */
    @Operation(
        summary = "Obtener producto por ID",
        description = "Recupera la información completa de un producto por su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado",
            content = @Content(schema = @Schema(implementation = ProductoResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public Mono<ProductoResponse> obtenerProducto(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable String id) {
        
        log.info("Solicitud para obtener producto con ID: {}", id);
        
        return buscarProductoUseCase.ejecutarPorId(id)
                .map(ProductoResponse::fromDomain);
    }

    /**
     * Obtiene todos los productos.
     * 
     * GET /api/v1/productos
     * 
     * Demuestra:
     * - Retorno de Flux para colecciones
     * - Streaming de respuestas con MediaType.APPLICATION_NDJSON_VALUE
     */
    @Operation(
        summary = "Listar todos los productos",
        description = "Obtiene una lista de todos los productos disponibles en el sistema"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lista de productos recuperada exitosamente"
    )
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_NDJSON_VALUE})
    public Flux<ProductoResponse> listarProductos() {
        log.info("Solicitud para listar todos los productos");
        
        return buscarProductoUseCase.ejecutarTodos()
                // map se aplica a cada elemento del Flux
                .map(ProductoResponse::fromDomain)
                .doOnComplete(() -> log.info("Listado de productos completado"));
    }

    /**
     * Busca productos por categoría.
     * 
     * GET /api/v1/productos/categoria/{categoria}
     * 
     * Demuestra:
     * - Uso de @PathVariable para parámetros de ruta
     * - Filtrado en la capa de aplicación
     */
    @Operation(
        summary = "Buscar productos por categoría",
        description = "Obtiene todos los productos de una categoría específica"
    )
    @GetMapping("/categoria/{categoria}")
    public Flux<ProductoResponse> buscarPorCategoria(
            @Parameter(description = "Categoría a buscar", example = "Electrónica")
            @PathVariable String categoria) {
        
        log.info("Buscando productos de categoría: {}", categoria);
        
        return buscarProductoUseCase.ejecutarPorCategoria(categoria)
                .map(ProductoResponse::fromDomain);
    }

    /**
     * Busca productos por nombre.
     * 
     * GET /api/v1/productos/buscar?nombre=texto
     * 
     * Demuestra:
     * - Uso de @RequestParam para query parameters
     * - Búsqueda de texto con Elasticsearch
     */
    @Operation(
        summary = "Buscar productos por nombre",
        description = "Realiza una búsqueda de texto completo en los nombres de productos"
    )
    @GetMapping("/buscar")
    public Flux<ProductoResponse> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre", example = "laptop")
            @RequestParam String nombre) {
        
        log.info("Buscando productos con nombre: {}", nombre);
        
        return buscarProductoUseCase.ejecutarPorNombre(nombre)
                .map(ProductoResponse::fromDomain);
    }

    /**
     * Actualiza un producto existente.
     * 
     * PUT /api/v1/productos/{id}
     * 
     * Demuestra:
     * - @PutMapping para HTTP PUT
     * - Actualización completa del recurso
     */
    @Operation(
        summary = "Actualizar producto",
        description = "Actualiza toda la información de un producto existente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto actualizado exitosamente"
        ),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PutMapping("/{id}")
    public Mono<ProductoResponse> actualizarProducto(
            @Parameter(description = "ID del producto a actualizar")
            @PathVariable String id,
            @Valid @RequestBody ActualizarProductoRequest request) {
        
        log.info("Actualizando producto con ID: {}", id);
        
        return actualizarProductoUseCase.ejecutar(
                id,
                request.nombre(),
                request.descripcion(),
                request.precio(),
                request.stock(),
                request.categoria(),
                request.activo()
        )
        .map(ProductoResponse::fromDomain)
        .doOnSuccess(response -> 
            log.info("Producto actualizado: {}", response.id())
        );
    }

    /**
     * Actualiza solo el stock de un producto.
     * 
     * PATCH /api/v1/productos/{id}/stock
     * 
     * Demuestra:
     * - @PatchMapping para actualizaciones parciales
     * - Operación especializada de stock
     */
    @Operation(
        summary = "Actualizar stock del producto",
        description = "Actualiza únicamente la cantidad en inventario de un producto"
    )
    @PatchMapping("/{id}/stock")
    public Mono<ProductoResponse> actualizarStock(
            @PathVariable String id,
            @Valid @RequestBody ActualizarStockRequest request) {
        
        log.info("Actualizando stock del producto {} a {}", id, request.stock());
        
        return actualizarProductoUseCase.actualizarStock(id, request.stock())
                .map(ProductoResponse::fromDomain);
    }

    /**
     * Elimina un producto.
     * 
     * DELETE /api/v1/productos/{id}
     * 
     * Demuestra:
     * - @DeleteMapping para HTTP DELETE
     * - Retorno de Mono<Void> para operaciones sin respuesta
     * - @ResponseStatus para código 204 (No Content)
     */
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto del sistema de forma permanente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminarProducto(@PathVariable String id) {
        log.info("Eliminando producto con ID: {}", id);
        
        return eliminarProductoUseCase.ejecutar(id)
                .doOnSuccess(v -> log.info("Producto eliminado: {}", id));
    }

    /**
     * Busca productos con stock mínimo.
     * 
     * GET /api/v1/productos/stock-minimo/{stock}
     * 
     * Demuestra:
     * - Endpoint personalizado para lógica de negocio específica
     */
    @Operation(
        summary = "Buscar productos con stock disponible",
        description = "Obtiene productos que tienen al menos la cantidad de stock especificada"
    )
    @GetMapping("/stock-minimo/{stock}")
    public Flux<ProductoResponse> buscarConStock(
            @Parameter(description = "Stock mínimo requerido", example = "10")
            @PathVariable Integer stock) {
        
        log.info("Buscando productos con stock mínimo: {}", stock);
        
        return buscarProductoUseCase.ejecutarConStock(stock)
                .map(ProductoResponse::fromDomain);
    }
}
