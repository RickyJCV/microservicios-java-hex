package com.microservicios.pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@SpringBootApplication
@EnableReactiveElasticsearchRepositories
public class PedidosServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PedidosServiceApplication.class, args);
    }
}
