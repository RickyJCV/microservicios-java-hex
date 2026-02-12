package com.microservicios.productos.infrastructure.adapter.rest;

import com.microservicios.productos.application.usecase.*;
import com.microservicios.productos.domain.model.Producto;
import com.microservicios.productos.infrastructure.adapter.rest.dto.ProductoDTO.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test de integración para ProductoController.
 * 
 * @WebFluxTest se usa para tests de capa web con WebFlux.
 * Configura automáticamente:
 * - WebTestClient para hacer peticiones HTTP de prueba
 * - Solo los componentes web (controllers, @ControllerAdvice, etc.)
 * - Deshabilita la configuración completa de Spring Boot
 * 
 * Demuestra:
 * - Testing de endpoints REST con WebFlux
 * - Uso de WebTestClient para peticiones reactivas
 * - Mocking de casos de uso
 * - Verificación de respuestas HTTP
 */
@WebFluxTest(ProductoController.class)
@DisplayName("Tests de integración para ProductoController")
class ProductoControllerIntegrationTest {

    // WebTestClient para hacer peticiones HTTP de prueba
    @Autowired
    private WebTestClient webTestClient;

    // Mocks de los casos de uso (Spring Boot los crea automáticamente)
    @MockBean
    private CrearProductoUseCase crearProductoUseCase;

    @MockBean
    private BuscarProductoUseCase buscarProductoUseCase;

    @MockBean
    private ActualizarProductoUseCase actualizarProductoUseCase;

    @MockBean
    private EliminarProductoUseCase eliminarProductoUseCase;

    /**
     * Test del endpoint POST /api/v1/productos
     * Verifica la creación de un producto.
     * 
     * Demuestra:
     * - POST request con WebTestClient
     * - Envío de body JSON
     * - Verificación de status code
     * - Verificación de headers
     * - Verificación de response body usando jsonPath
     */
    @Test
    @DisplayName("POST /api/v1/productos debe crear un producto y retornar 201 Created")
    void debeCrearProductoYRetornar201() {
        // Given - Preparar datos de prueba
        CrearProductoRequest request = new CrearProductoRequest(
                "Laptop Dell XPS 15",
                "Laptop de alto rendimiento",
                new BigDecimal("1299.99"),
                50,
                "Electrónica"
        );

        Producto productoCreado = new Producto(
                "123",
                request.nombre(),
                request.descripcion(),
                request.precio(),
                request.stock(),
                request.categoria(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Configurar el mock del caso de uso
        when(crearProductoUseCase.ejecutar(
                anyString(), anyString(), any(BigDecimal.class), anyInt(), anyString()
        )).thenReturn(Mono.just(productoCreado));

        // When/Then - Ejecutar petición y verificar respuesta
        webTestClient.post()
                .uri("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)  // Enviar request body
                .exchange()  // Ejecutar la petición
                // Verificaciones de la respuesta
                .expectStatus().isCreated()  // Verificar status 201
                .expectHeader().contentType(MediaType.APPLICATION_JSON)  // Verificar content-type
                .expectBody()
                // jsonPath permite verificar campos específicos del JSON
                .jsonPath("$.id").isEqualTo("123")
                .jsonPath("$.nombre").isEqualTo(request.nombre())
                .jsonPath("$.precio").isEqualTo(1299.99)
                .jsonPath("$.stock").isEqualTo(50)
                .jsonPath("$.activo").isEqualTo(true);
    }

    /**
     * Test del endpoint GET /api/v1/productos/{id}
     * Verifica la obtención de un producto por ID.
     */
    @Test
    @DisplayName("GET /api/v1/productos/{id} debe retornar el producto con status 200")
    void debeObtenerProductoPorIdYRetornar200() {
        // Given
        String productoId = "123";
        Producto producto = new Producto(
                productoId,
                "Laptop Dell XPS 15",
                "Laptop de alto rendimiento",
                new BigDecimal("1299.99"),
                50,
                "Electrónica",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(buscarProductoUseCase.ejecutarPorId(productoId))
                .thenReturn(Mono.just(producto));

        // When/Then
        webTestClient.get()
                .uri("/api/v1/productos/{id}", productoId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(productoId)
                .jsonPath("$.nombre").isEqualTo("Laptop Dell XPS 15");
    }

    /**
     * Test del endpoint GET /api/v1/productos/{id} cuando no existe.
     * Verifica el manejo de errores 404.
     */
    @Test
    @DisplayName("GET /api/v1/productos/{id} debe retornar 404 cuando no existe")
    void debeRetornar404CuandoProductoNoExiste() {
        // Given
        String productoId = "999";
        when(buscarProductoUseCase.ejecutarPorId(productoId))
                .thenReturn(Mono.error(
                    new BuscarProductoUseCase.ProductoNoEncontradoException("Producto no encontrado")
                ));

        // When/Then
        webTestClient.get()
                .uri("/api/v1/productos/{id}", productoId)
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Test del endpoint GET /api/v1/productos
     * Verifica la obtención de todos los productos.
     * 
     * Demuestra:
     * - Testing de endpoints que retornan Flux
     * - Verificación de listas en response
     */
    @Test
    @DisplayName("GET /api/v1/productos debe retornar lista de productos")
    void debeListarTodosLosProductos() {
        // Given - Crear lista de productos de prueba
        Producto producto1 = new Producto(
                "1", "Producto 1", "Descripción 1",
                new BigDecimal("100"), 10, "Cat1",
                true, LocalDateTime.now(), LocalDateTime.now()
        );
        Producto producto2 = new Producto(
                "2", "Producto 2", "Descripción 2",
                new BigDecimal("200"), 20, "Cat2",
                true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(buscarProductoUseCase.ejecutarTodos())
                .thenReturn(Flux.just(producto1, producto2));

        // When/Then
        webTestClient.get()
                .uri("/api/v1/productos")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductoResponse.class)
                .hasSize(2);  // Verificar que hay 2 elementos
    }

    /**
     * Test del endpoint PUT /api/v1/productos/{id}
     * Verifica la actualización de un producto.
     */
    @Test
    @DisplayName("PUT /api/v1/productos/{id} debe actualizar el producto")
    void debeActualizarProducto() {
        // Given
        String productoId = "123";
        ActualizarProductoRequest request = new ActualizarProductoRequest(
                "Laptop Actualizada",
                "Nueva descripción",
                new BigDecimal("1199.99"),
                45,
                "Electrónica",
                true
        );

        Producto productoActualizado = new Producto(
                productoId,
                request.nombre(),
                request.descripcion(),
                request.precio(),
                request.stock(),
                request.categoria(),
                request.activo(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );

        when(actualizarProductoUseCase.ejecutar(
                eq(productoId), anyString(), anyString(), 
                any(BigDecimal.class), anyInt(), anyString(), anyBoolean()
        )).thenReturn(Mono.just(productoActualizado));

        // When/Then
        webTestClient.put()
                .uri("/api/v1/productos/{id}", productoId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(productoId)
                .jsonPath("$.nombre").isEqualTo(request.nombre())
                .jsonPath("$.precio").isEqualTo(1199.99);
    }

    /**
     * Test del endpoint DELETE /api/v1/productos/{id}
     * Verifica la eliminación de un producto.
     */
    @Test
    @DisplayName("DELETE /api/v1/productos/{id} debe eliminar el producto")
    void debeEliminarProducto() {
        // Given
        String productoId = "123";
        when(eliminarProductoUseCase.ejecutar(productoId))
                .thenReturn(Mono.empty());

        // When/Then
        webTestClient.delete()
                .uri("/api/v1/productos/{id}", productoId)
                .exchange()
                .expectStatus().isNoContent();  // Verificar 204 No Content
    }

    /**
     * Test del endpoint GET /api/v1/productos/categoria/{categoria}
     * Verifica la búsqueda por categoría.
     */
    @Test
    @DisplayName("GET /api/v1/productos/categoria/{categoria} debe buscar por categoría")
    void debeBuscarPorCategoria() {
        // Given
        String categoria = "Electrónica";
        Producto producto = new Producto(
                "1", "Producto Electrónico", "Descripción",
                new BigDecimal("500"), 15, categoria,
                true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(buscarProductoUseCase.ejecutarPorCategoria(categoria))
                .thenReturn(Flux.just(producto));

        // When/Then
        webTestClient.get()
                .uri("/api/v1/productos/categoria/{categoria}", categoria)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductoResponse.class)
                .hasSize(1);
    }
}
