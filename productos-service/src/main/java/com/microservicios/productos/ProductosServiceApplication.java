package com.microservicios.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

/**
 * Clase principal de la aplicación Spring Boot - Microservicio de Productos.
 * 
 * @SpringBootApplication es una anotación combinada que incluye:
 * - @Configuration: Marca la clase como fuente de definiciones de beans
 * - @EnableAutoConfiguration: Habilita la configuración automática de Spring Boot
 * - @ComponentScan: Escanea el paquete actual y subpaquetes buscando componentes
 * 
 * @EnableReactiveElasticsearchRepositories habilita los repositorios reactivos de Elasticsearch.
 * Spring Data creará implementaciones automáticas de las interfaces de repositorio.
 */
@SpringBootApplication
@EnableReactiveElasticsearchRepositories
public class ProductosServiceApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     * 
     * SpringApplication.run() realiza las siguientes acciones:
     * 1. Crea un ApplicationContext apropiado
     * 2. Registra un CommandLinePropertySource para exponer argumentos como propiedades
     * 3. Refresca el ApplicationContext, cargando todos los beans
     * 4. Dispara cualquier CommandLineRunner beans
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductosServiceApplication.class, args);
    }
}
