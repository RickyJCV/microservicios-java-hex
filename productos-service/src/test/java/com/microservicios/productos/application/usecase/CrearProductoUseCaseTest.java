package com.microservicios.productos.application.usecase;

import com.microservicios.productos.domain.model.Producto;
import com.microservicios.productos.domain.port.ProductoEventPublisher;
import com.microservicios.productos.domain.port.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitario para CrearProductoUseCase.
 * 
 * Demuestra TDD (Test-Driven Development) y testing de código reactivo.
 * 
 * Anotaciones JUnit 5:
 * - @ExtendWith(MockitoExtension.class): Habilita Mockito para crear mocks
 * - @Mock: Crea un mock del objeto
 * - @InjectMocks: Crea la instancia e inyecta los mocks
 * - @Test: Marca un método como test
 * - @DisplayName: Proporciona un nombre descriptivo para el test
 * - @BeforeEach: Se ejecuta antes de cada test
 * 
 * StepVerifier: Utilidad de Project Reactor para testing de flujos reactivos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para CrearProductoUseCase")
class CrearProductoUseCaseTest {

    // Mocks de las dependencias
    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProductoEventPublisher eventPublisher;

    // Clase a testear (se inyectan los mocks)
    @InjectMocks
    private CrearProductoUseCase crearProductoUseCase;

    // Datos de prueba reutilizables
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String categoria;

    /**
     * Configuración que se ejecuta antes de cada test.
     * Inicializa los datos de prueba.
     */
    @BeforeEach
    void setUp() {
        nombre = "Laptop Dell XPS 15";
        descripcion = "Laptop de alto rendimiento";
        precio = new BigDecimal("1299.99");
        stock = 50;
        categoria = "Electrónica";
    }

    /**
     * Test del camino feliz (happy path).
     * Verifica que un producto se crea correctamente cuando todo funciona bien.
     * 
     * Demuestra:
     * - Uso de Mockito para definir comportamiento de mocks
     * - StepVerifier para verificar flujos reactivos
     * - AssertJ para aserciones fluidas
     */
    @Test
    @DisplayName("Debe crear un producto exitosamente")
    void debeCrearProductoExitosamente() {
        // Given (Dado) - Preparar el escenario de prueba
        // Crear un producto de ejemplo que será retornado por el repositorio
        Producto productoEsperado = new Producto(
                "123",
                nombre,
                descripcion,
                precio,
                stock,
                categoria,
                true,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now()
        );

        // Configurar el comportamiento del mock del repositorio
        // when(...).thenReturn(...) define qué retornar cuando se llama al método
        when(productoRepository.guardar(any(Producto.class)))
                .thenReturn(Mono.just(productoEsperado));

        // Configurar el mock del publisher para que retorne Mono.empty()
        when(eventPublisher.publicarProductoCreado(any(Producto.class)))
                .thenReturn(Mono.empty());

        // When (Cuando) - Ejecutar la acción a testear
        Mono<Producto> resultado = crearProductoUseCase.ejecutar(
                nombre, descripcion, precio, stock, categoria
        );

        // Then (Entonces) - Verificar los resultados
        // StepVerifier prueba flujos reactivos paso a paso
        StepVerifier.create(resultado)
                .assertNext(producto -> {
                    // Verificar que el producto tiene los valores correctos
                    assertThat(producto).isNotNull();
                    assertThat(producto.id()).isEqualTo("123");
                    assertThat(producto.nombre()).isEqualTo(nombre);
                    assertThat(producto.descripcion()).isEqualTo(descripcion);
                    assertThat(producto.precio()).isEqualByComparingTo(precio);
                    assertThat(producto.stock()).isEqualTo(stock);
                    assertThat(producto.categoria()).isEqualTo(categoria);
                    assertThat(producto.activo()).isTrue();
                })
                .verifyComplete(); // Verifica que el flujo completó exitosamente

        // Verificar que se llamaron los métodos esperados
        // verify() confirma que el mock fue invocado
        verify(productoRepository, times(1)).guardar(any(Producto.class));
        verify(eventPublisher, times(1)).publicarProductoCreado(any(Producto.class));
    }

    /**
     * Test que verifica el manejo de errores.
     * Demuestra testing de caminos de error en flujos reactivos.
     */
    @Test
    @DisplayName("Debe propagar error cuando el repositorio falla")
    void debePropagingErrorCuandoRepositorioFalla() {
        // Given - Configurar el repositorio para que lance un error
        RuntimeException error = new RuntimeException("Error de base de datos");
        when(productoRepository.guardar(any(Producto.class)))
                .thenReturn(Mono.error(error));

        // When - Ejecutar el caso de uso
        Mono<Producto> resultado = crearProductoUseCase.ejecutar(
                nombre, descripcion, precio, stock, categoria
        );

        // Then - Verificar que se propaga el error
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Error de base de datos")
                )
                .verify();

        // Verificar que no se publicó el evento (porque falló antes)
        verify(eventPublisher, never()).publicarProductoCreado(any());
    }

    /**
     * Test que verifica validación de precio negativo.
     * Demuestra testing de validaciones de negocio.
     */
    @Test
    @DisplayName("Debe fallar cuando el precio es negativo")
    void debeFallarCuandoPrecioEsNegativo() {
        // Given - Precio inválido
        BigDecimal precioNegativo = new BigDecimal("-100");

        // When/Then - Verificar que se lanza excepción
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Producto.crear(nombre, descripcion, precioNegativo, stock, categoria),
                "El precio debe ser mayor o igual a cero"
        );
    }

    /**
     * Test que verifica validación de stock negativo.
     */
    @Test
    @DisplayName("Debe fallar cuando el stock es negativo")
    void debeFallarCuandoStockEsNegativo() {
        // Given - Stock inválido
        Integer stockNegativo = -10;

        // When/Then - Verificar que se lanza excepción
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Producto.crear(nombre, descripcion, precio, stockNegativo, categoria),
                "El stock no puede ser negativo"
        );
    }

    /**
     * Test que verifica validación de nombre vacío.
     */
    @Test
    @DisplayName("Debe fallar cuando el nombre está vacío")
    void debeFallarCuandoNombreEstaVacio() {
        // When/Then - Verificar que se lanza excepción con nombre vacío
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Producto.crear("", descripcion, precio, stock, categoria),
                "El nombre del producto no puede estar vacío"
        );
    }
}
