package com.microservicios.productos.application.usecase;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microservicios.productos.domain.model.Producto;
import com.microservicios.productos.domain.port.ProductoEventPublisher;
import com.microservicios.productos.domain.port.ProductoRepository;

import reactor.core.publisher.Mono;

/**
 * Caso de uso para crear un nuevo producto.
 * Los casos de uso representan la lógica de negocio de la aplicación.
 * 
 * Anotaciones utilizadas:
 * - @Service: Marca la clase como un componente de servicio de Spring
 * - @RequiredArgsConstructor: Lombok genera un constructor con los campos final
 * - @Slf4j: Lombok genera un logger automáticamente
 */
@Service
public class CrearProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CrearProductoUseCase.class);

    private final ProductoRepository productoRepository;
    private final ProductoEventPublisher eventPublisher;

    public CrearProductoUseCase(ProductoRepository productoRepository, ProductoEventPublisher eventPublisher) {
        this.productoRepository = productoRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Ejecuta el caso de uso de creación de producto.
     * 
     * Flujo:
     * 1. Crea el producto usando el método de fábrica
     * 2. Guarda el producto en el repositorio
     * 3. Publica un evento de producto creado
     * 4. Registra la operación en los logs
     * 
     * El uso de flatMap es clave en programación reactiva:
     * - map: transforma el valor
     * - flatMap: transforma el valor y "aplana" el Mono/Flux resultante
     *
     * @param nombre Nombre del producto
     * @param descripcion Descripción del producto
     * @param precio Precio del producto
     * @param stock Stock inicial
     * @param categoria Categoría del producto
     * @return Mono que emite el producto creado
     */
    public Mono<Producto> ejecutar(String nombre, String descripcion, BigDecimal precio, 
                                   Integer stock, String categoria) {
        
        log.info("Iniciando creación de producto: {}", nombre);
        
        // Crear el producto usando el método de fábrica del dominio
        Producto nuevoProducto = Producto.crear(nombre, descripcion, precio, stock, categoria);
        
        // Flujo reactivo: guardar -> publicar evento -> registrar log
        return productoRepository.guardar(nuevoProducto)
                .flatMap(productoGuardado -> 
                    // flatMap se usa aquí porque eventPublisher retorna Mono<Void>
                    // y queremos continuar con el producto guardado
                    eventPublisher.publicarProductoCreado(productoGuardado)
                            .thenReturn(productoGuardado) // thenReturn ignora el resultado anterior y retorna el producto
                )
                .doOnSuccess(producto -> 
                    // doOnSuccess ejecuta una acción sin modificar el flujo reactivo
                    log.info("Producto creado exitosamente con ID: {}", producto.id())
                )
                .doOnError(error -> 
                    // doOnError se ejecuta si hay un error en cualquier paso
                    log.error("Error al crear producto: {}", error.getMessage(), error)
                );
    }
}
