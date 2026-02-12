# 🚀 Guía de Inicio Rápido

## Pasos para ejecutar el proyecto en 5 minutos

### 1. Requisitos Previos
- Java 21 instalado
- Maven 3.8+ instalado
- Docker y Docker Compose instalados

### 2. Levantar Infraestructura (Elasticsearch y RabbitMQ)

```bash
# Desde la raíz del proyecto
docker-compose up -d

# Verificar que están corriendo
docker-compose ps
```

**Espera 30-60 segundos** para que Elasticsearch y RabbitMQ inicien completamente.

### 3. Compilar los Microservicios

```bash
# Compilar productos-service
cd productos-service
mvn clean install
cd ..

# Compilar pedidos-service
cd pedidos-service
mvn clean install
cd ..
```

### 4. Ejecutar los Microservicios

**Terminal 1 - Productos Service:**
```bash
cd productos-service
mvn spring-boot:run
```

**Terminal 2 - Pedidos Service:**
```bash
cd pedidos-service
mvn spring-boot:run
```

### 5. Verificar que todo funciona

Abre tu navegador en:
- **Swagger Productos**: http://localhost:8080/swagger-ui.html
- **Swagger Pedidos**: http://localhost:8081/swagger-ui.html
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Elasticsearch**: http://localhost:9200

### 6. Probar la API

#### Crear un producto:
```bash
curl -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Laptop Dell XPS 15",
    "descripcion": "Laptop de alto rendimiento",
    "precio": 1299.99,
    "stock": 50,
    "categoria": "Electrónica"
  }'
```

Guarda el `id` del producto retornado.

#### Listar productos:
```bash
curl http://localhost:8080/api/v1/productos
```

#### Crear un pedido (reemplaza PRODUCTO_ID):
```bash
curl -X POST http://localhost:8081/api/v1/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI001",
    "clienteNombre": "Juan Pérez",
    "items": [
      {
        "productoId": "PRODUCTO_ID_AQUI",
        "cantidad": 2
      }
    ]
  }'
```

## 🎯 Próximos Pasos

1. Explora la documentación Swagger UI
2. Revisa el código fuente comentado
3. Ejecuta los tests: `mvn test`
4. Modifica y experimenta con el código

## ⚠️ Solución de Problemas Comunes

**Puerto 8080 o 8081 ocupado:**
```bash
# Ver qué proceso usa el puerto
lsof -i :8080
# Matar el proceso
kill -9 [PID]
```

**Elasticsearch no responde:**
```bash
# Reiniciar contenedor
docker-compose restart elasticsearch
# Ver logs
docker-compose logs elasticsearch
```

**Error de compilación:**
```bash
# Limpiar y recompilar
mvn clean install -U
```

## 📖 Más Información

Consulta el README.md principal para documentación completa.

¡Disfruta explorando el proyecto! 🎉
