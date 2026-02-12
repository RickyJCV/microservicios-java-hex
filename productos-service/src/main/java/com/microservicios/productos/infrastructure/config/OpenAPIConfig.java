package com.microservicios.productos.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI (Swagger) para documentación de la API.
 * 
 * OpenAPI/Swagger proporciona:
 * - Documentación interactiva de la API
 * - Interfaz de prueba de endpoints (Swagger UI)
 * - Especificación en formato JSON/YAML
 * 
 * La documentación estará disponible en:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenAPIConfig {

    /**
     * Configuración principal de OpenAPI.
     * Define la información de la API, servidores, contacto, licencia, etc.
     * 
     * @return Objeto OpenAPI configurado
     */
    @Bean
    public OpenAPI productosOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList());
    }

    /**
     * Información general de la API.
     * Aparece en la parte superior de la documentación Swagger.
     * 
     * @return Info con metadatos de la API
     */
    private Info apiInfo() {
        return new Info()
                .title("API de Gestión de Productos")
                .description("""
                    API RESTful para la gestión completa de productos.
                    
                    **Características:**
                    - Operaciones CRUD completas
                    - Arquitectura Hexagonal
                    - Programación Reactiva con WebFlux
                    - Integración con Elasticsearch para búsquedas
                    - Mensajería asíncrona con RabbitMQ
                    - Documentación OpenAPI/Swagger
                    
                    **Casos de uso:**
                    - Crear nuevos productos
                    - Buscar productos por ID, nombre o categoría
                    - Actualizar información de productos
                    - Gestionar inventario (stock)
                    - Eliminar productos
                    
                    **Tecnologías:**
                    - Java 21
                    - Spring Boot 3.2.1
                    - Spring WebFlux
                    - Spring Data Elasticsearch
                    - RabbitMQ
                    - Lombok
                    """)
                .version("1.0.0")
                .contact(contact())
                .license(license());
    }

    /**
     * Información de contacto del equipo de desarrollo.
     * 
     * @return Contact con datos de contacto
     */
    private Contact contact() {
        return new Contact()
                .name("Equipo de Desarrollo")
                .email("desarrollo@microservicios.com")
                .url("https://github.com/microservicios/productos-service");
    }

    /**
     * Información de licencia del proyecto.
     * 
     * @return License configurada
     */
    private License license() {
        return new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

    /**
     * Lista de servidores donde está desplegada la API.
     * Útil cuando hay múltiples entornos (dev, staging, prod).
     * 
     * @return Lista de servidores
     */
    private List<Server> serverList() {
        // Servidor de desarrollo local
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Servidor de Desarrollo Local");

        // Servidor de staging (comentado como ejemplo)
        Server stagingServer = new Server()
                .url("http://staging.microservicios.com")
                .description("Servidor de Staging");

        // Servidor de producción (comentado como ejemplo)
        Server prodServer = new Server()
                .url("https://api.microservicios.com")
                .description("Servidor de Producción");

        // Por ahora solo retornamos el servidor de desarrollo
        return List.of(devServer);
    }
}
