package com.microservicios.productos.infrastructure.adapter.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicios.productos.domain.model.Producto;
import com.microservicios.productos.domain.port.ProductoEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador que implementa el puerto ProductoEventPublisher usando RabbitMQ.
 * Publica eventos de productos para comunicación entre microservicios.
 * 
 * RabbitMQ es un broker de mensajes que implementa AMQP (Advanced Message Queuing Protocol).
 * Permite comunicación asíncrona y desacoplada entre microservicios.
 */
@Component
public class ProductoEventPublisherAdapter implements ProductoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductoEventPublisherAdapter.class);

    // RabbitTemplate para enviar mensajes (inyectado por Spring)
    private final RabbitTemplate rabbitTemplate;
    
    // ObjectMapper para serializar objetos a JSON
    private final ObjectMapper objectMapper;

    public ProductoEventPublisherAdapter(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    // Constantes para el exchange y routing keys
    private static final String EXCHANGE = "productos.exchange";
    private static final String ROUTING_KEY_CREADO = "producto.creado";
    private static final String ROUTING_KEY_ACTUALIZADO = "producto.actualizado";
    private static final String ROUTING_KEY_ELIMINADO = "producto.eliminado";
    private static final String ROUTING_KEY_STOCK = "producto.stock.cambio";

    /**
     * Publica evento de producto creado.
     * 
     * Demuestra:
     * - Serialización a JSON
     * - Publicación asíncrona con Mono.fromRunnable
     * - Uso de Schedulers para operaciones bloqueantes
     */
    @Override
    public Mono<Void> publicarProductoCreado(Producto producto) {
        log.info("Publicando evento producto creado: {}", producto.id());
        
        // Crear el mensaje como Map para flexibilidad
        Map<String, Object> mensaje = crearMensajeProducto(producto);
        mensaje.put("evento", "PRODUCTO_CREADO");
        
        // Mono.fromRunnable ejecuta código imperativo de forma reactiva
        // subscribeOn(Schedulers.boundedElastic()) ejecuta en thread pool para operaciones bloqueantes
        return Mono.fromRunnable(() -> {
            try {
                // convertAndSend es bloqueante, por eso usamos Schedulers
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CREADO, mensaje);
                log.debug("Evento publicado exitosamente para producto: {}", producto.id());
            } catch (Exception e) {
                log.error("Error al publicar evento de producto creado", e);
                throw new RuntimeException("Error al publicar evento", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Publica evento de producto actualizado.
     */
    @Override
    public Mono<Void> publicarProductoActualizado(Producto producto) {
        log.info("Publicando evento producto actualizado: {}", producto.id());
        
        Map<String, Object> mensaje = crearMensajeProducto(producto);
        mensaje.put("evento", "PRODUCTO_ACTUALIZADO");
        
        return Mono.fromRunnable(() -> {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_ACTUALIZADO, mensaje);
                log.debug("Evento de actualización publicado: {}", producto.id());
            } catch (Exception e) {
                log.error("Error al publicar evento de actualización", e);
                throw new RuntimeException("Error al publicar evento", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Publica evento de producto eliminado.
     */
    @Override
    public Mono<Void> publicarProductoEliminado(String productoId) {
        log.info("Publicando evento producto eliminado: {}", productoId);
        
        // Para eliminación, solo enviamos el ID y timestamp
        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("evento", "PRODUCTO_ELIMINADO");
        mensaje.put("productoId", productoId);
        mensaje.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return Mono.fromRunnable(() -> {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_ELIMINADO, mensaje);
                log.debug("Evento de eliminación publicado: {}", productoId);
            } catch (Exception e) {
                log.error("Error al publicar evento de eliminación", e);
                throw new RuntimeException("Error al publicar evento", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Publica evento específico de cambio de stock.
     * Útil para microservicios que necesitan reaccionar a cambios de inventario.
     */
    @Override
    public Mono<Void> publicarCambioStock(String productoId, Integer stockAnterior, Integer stockNuevo) {
        log.info("Publicando cambio de stock para producto {}: {} -> {}", 
                productoId, stockAnterior, stockNuevo);
        
        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("evento", "PRODUCTO_STOCK_CAMBIO");
        mensaje.put("productoId", productoId);
        mensaje.put("stockAnterior", stockAnterior);
        mensaje.put("stockNuevo", stockNuevo);
        mensaje.put("diferencia", stockNuevo - stockAnterior);
        mensaje.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return Mono.fromRunnable(() -> {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_STOCK, mensaje);
                log.debug("Evento de cambio de stock publicado: {}", productoId);
            } catch (Exception e) {
                log.error("Error al publicar evento de cambio de stock", e);
                throw new RuntimeException("Error al publicar evento", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Método auxiliar para crear el mensaje del producto.
     * Usa ObjectMapper para convertir el producto a Map.
     * 
     * Demuestra:
     * - Uso de ObjectMapper para serialización
     * - Manejo de excepciones con SuppressWarnings cuando es seguro
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> crearMensajeProducto(Producto producto) {
        try {
            // Convertir Producto a Map usando ObjectMapper
            String json = objectMapper.writeValueAsString(producto);
            Map<String, Object> mensaje = objectMapper.readValue(json, HashMap.class);
            mensaje.put("timestamp", java.time.LocalDateTime.now().toString());
            return mensaje;
        } catch (Exception e) {
            log.error("Error al serializar producto", e);
            throw new RuntimeException("Error al crear mensaje", e);
        }
    }
}
