package com.microservicios.productos.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para el microservicio de productos.
 * 
 * Define la topología de mensajería:
 * - Exchange: punto de entrada de mensajes
 * - Queues: colas donde se almacenan los mensajes
 * - Bindings: conexiones entre exchange y queues con routing keys
 * 
 * @Configuration indica que esta clase define beans de Spring
 */
@Configuration
public class RabbitMQConfig {

    // Nombres de los componentes de RabbitMQ
    public static final String EXCHANGE_NAME = "productos.exchange";
    public static final String QUEUE_CREADO = "productos.creado.queue";
    public static final String QUEUE_ACTUALIZADO = "productos.actualizado.queue";
    public static final String QUEUE_ELIMINADO = "productos.eliminado.queue";
    public static final String QUEUE_STOCK = "productos.stock.queue";
    
    // Routing keys para enrutamiento de mensajes
    public static final String ROUTING_KEY_CREADO = "producto.creado";
    public static final String ROUTING_KEY_ACTUALIZADO = "producto.actualizado";
    public static final String ROUTING_KEY_ELIMINADO = "producto.eliminado";
    public static final String ROUTING_KEY_STOCK = "producto.stock.cambio";

    /**
     * Define el Exchange tipo Topic.
     * 
     * Topic Exchange permite routing flexible con patrones:
     * - producto.* matchea producto.creado, producto.actualizado, etc.
     * - producto.# matchea cualquier cosa que empiece con producto.
     * 
     * @return TopicExchange configurado
     */
    @Bean
    public TopicExchange productosExchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_NAME)
                .durable(true) // El exchange sobrevive a reinicios del broker
                .build();
    }

    /**
     * Cola para eventos de productos creados.
     * 
     * @return Queue configurada
     */
    @Bean
    public Queue productoCreadoQueue() {
        return QueueBuilder
                .durable(QUEUE_CREADO) // La cola persiste mensajes en disco
                .build();
    }

    /**
     * Cola para eventos de productos actualizados.
     */
    @Bean
    public Queue productoActualizadoQueue() {
        return QueueBuilder
                .durable(QUEUE_ACTUALIZADO)
                .build();
    }

    /**
     * Cola para eventos de productos eliminados.
     */
    @Bean
    public Queue productoEliminadoQueue() {
        return QueueBuilder
                .durable(QUEUE_ELIMINADO)
                .build();
    }

    /**
     * Cola para eventos de cambio de stock.
     */
    @Bean
    public Queue productoStockQueue() {
        return QueueBuilder
                .durable(QUEUE_STOCK)
                .build();
    }

    /**
     * Binding que conecta la queue de productos creados con el exchange.
     * 
     * @param productoCreadoQueue Queue a bindear
     * @param productosExchange Exchange destino
     * @return Binding configurado
     */
    @Bean
    public Binding bindingProductoCreado(Queue productoCreadoQueue, TopicExchange productosExchange) {
        return BindingBuilder
                .bind(productoCreadoQueue)
                .to(productosExchange)
                .with(ROUTING_KEY_CREADO);
    }

    /**
     * Binding para productos actualizados.
     */
    @Bean
    public Binding bindingProductoActualizado(Queue productoActualizadoQueue, TopicExchange productosExchange) {
        return BindingBuilder
                .bind(productoActualizadoQueue)
                .to(productosExchange)
                .with(ROUTING_KEY_ACTUALIZADO);
    }

    /**
     * Binding para productos eliminados.
     */
    @Bean
    public Binding bindingProductoEliminado(Queue productoEliminadoQueue, TopicExchange productosExchange) {
        return BindingBuilder
                .bind(productoEliminadoQueue)
                .to(productosExchange)
                .with(ROUTING_KEY_ELIMINADO);
    }

    /**
     * Binding para cambios de stock.
     */
    @Bean
    public Binding bindingProductoStock(Queue productoStockQueue, TopicExchange productosExchange) {
        return BindingBuilder
                .bind(productoStockQueue)
                .to(productosExchange)
                .with(ROUTING_KEY_STOCK);
    }

    /**
     * Conversor de mensajes a JSON.
     * Convierte automáticamente objetos Java a JSON para envío por RabbitMQ.
     * 
     * @return MessageConverter que usa Jackson
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Template para enviar mensajes a RabbitMQ.
     * Configura el conversor JSON y otras opciones.
     * 
     * @param connectionFactory Factory de conexiones a RabbitMQ (autowired)
     * @return RabbitTemplate configurado
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
