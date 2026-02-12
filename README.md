# Proyecto Microservicios con Arquitectura Hexagonal

## 📋 Descripción General

Este proyecto implementa **dos microservicios** en Java 21 con Spring Boot 3.2.1, utilizando **arquitectura hexagonal** (también conocida como arquitectura de puertos y adaptadores). Los microservicios se comunican entre sí mediante **RabbitMQ** y utilizan **Elasticsearch** para almacenamiento y búsqueda de datos.

### Microservicios Incluidos

1. **productos-service** (Puerto: 8080)
   - Gestión completa de productos
   - CRUD de productos
   - Búsquedas avanzadas con Elasticsearch
   - Publicación de eventos de productos

2. **pedidos-service** (Puerto: 8081)
   - Gestión de pedidos
   - Validación de stock con productos-service
   - Actualización automática de inventario
   - Gestión de estados de pedidos

## 🏗️ Arquitectura Hexagonal

### ¿Qué es la Arquitectura Hexagonal?

La arquitectura hexagonal separa la lógica de negocio del dominio de los detalles de implementación técnica. Está organizada en tres capas principales:

```
┌─────────────────────────────────────────┐
│         CAPA DE DOMINIO                 │
│  (Lógica de Negocio - Sin dependencias) │
│  - Model: Entidades del dominio         │
│  - Port: Interfaces (contratos)         │
└─────────────────────────────────────────┘
              ↑           ↑
              │           │
    ┌─────────┴───┐   ┌──┴──────────┐
    │ APLICACIÓN  │   │ APLICACIÓN  │
    │ Use Cases   │   │ Use Cases   │
    └─────────────┘   └─────────────┘
              ↑           ↑
              │           │
┌─────────────┴───────────┴──────────────┐
│      CAPA DE INFRAESTRUCTURA           │
│  (Adaptadores - Detalles técnicos)     │
│  - REST Controllers                     │
│  - Elasticsearch Repositories           │
│  - RabbitMQ Publishers/Consumers       │
│  - WebClient para HTTP                  │
└────────────────────────────────────────┘
```

### Ventajas de esta Arquitectura

✅ **Testabilidad**: Fácil de testear unitariamente sin dependencias externas  
✅ **Mantenibilidad**: Cambios en infraestructura no afectan la lógica de negocio  
✅ **Independencia**: El dominio no depende de frameworks específicos  
✅ **Flexibilidad**: Fácil cambiar de base de datos, framework web, etc.

## 🚀 Tecnologías Utilizadas

### Core
- **Java 21**: Última versión LTS con Records, Pattern Matching, Switch Expressions
- **Spring Boot 3.2.1**: Framework principal
- **Maven**: Gestión de dependencias

### Programación Reactiva
- **Spring WebFlux**: Framework web reactivo (basado en Netty)
- **Project Reactor**: Librería de programación reactiva (Mono y Flux)

### Bases de Datos y Búsqueda
- **Elasticsearch 8.x**: Motor de búsqueda y almacenamiento de documentos
- **Spring Data Elasticsearch**: Integración reactiva con Elasticsearch

### Mensajería
- **RabbitMQ**: Message broker para comunicación asíncrona entre microservicios
- **Spring AMQP**: Integración con RabbitMQ

### Documentación
- **SpringDoc OpenAPI**: Generación automática de documentación API
- **Swagger UI**: Interfaz interactiva para probar endpoints

### Testing
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking de dependencias
- **Reactor Test**: Testing de código reactivo (StepVerifier)
- **WebTestClient**: Testing de endpoints REST reactivos
- **Testcontainers**: Contenedores Docker para tests de integración

### Utilidades
- **Lombok**: Reducción de código boilerplate
- **Jackson**: Serialización/deserialización JSON

## 📁 Estructura del Proyecto

```
microservicios-hexagonal/
├── productos-service/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/microservicios/productos/
│       │   │   ├── domain/
│       │   │   │   ├── model/           # Entidades del dominio (Records)
│       │   │   │   │   └── Producto.java
│       │   │   │   └── port/            # Puertos (interfaces)
│       │   │   │       ├── ProductoRepository.java
│       │   │   │       └── ProductoEventPublisher.java
│       │   │   ├── application/
│       │   │   │   └── usecase/         # Casos de uso (lógica de negocio)
│       │   │   │       ├── CrearProductoUseCase.java
│       │   │   │       ├── BuscarProductoUseCase.java
│       │   │   │       ├── ActualizarProductoUseCase.java
│       │   │   │       └── EliminarProductoUseCase.java
│       │   │   ├── infrastructure/
│       │   │   │   ├── adapter/
│       │   │   │   │   ├── rest/        # Controladores REST
│       │   │   │   │   │   ├── ProductoController.java
│       │   │   │   │   │   └── dto/
│       │   │   │   │   │       └── ProductoDTO.java
│       │   │   │   │   ├── elasticsearch/  # Adaptador Elasticsearch
│       │   │   │   │   │   ├── ProductoDocument.java
│       │   │   │   │   │   ├── ProductoElasticsearchRepository.java
│       │   │   │   │   │   └── ProductoRepositoryAdapter.java
│       │   │   │   │   └── messaging/      # Adaptador RabbitMQ
│       │   │   │   │       └── ProductoEventPublisherAdapter.java
│       │   │   │   └── config/          # Configuración Spring
│       │   │   │       ├── RabbitMQConfig.java
│       │   │   │       └── OpenAPIConfig.java
│       │   │   └── ProductosServiceApplication.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/                        # Tests unitarios e integración
│           └── java/com/microservicios/productos/
│               ├── application/usecase/
│               │   └── CrearProductoUseCaseTest.java
│               └── infrastructure/adapter/rest/
│                   └── ProductoControllerIntegrationTest.java
│
├── pedidos-service/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/microservicios/pedidos/
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   │   ├── Pedido.java
│       │   │   │   │   ├── ItemPedido.java
│       │   │   │   │   └── EstadoPedido.java
│       │   │   │   └── port/
│       │   │   │       ├── PedidoRepository.java
│       │   │   │       ├── PedidoEventPublisher.java
│       │   │   │       └── ProductoServiceClient.java
│       │   │   ├── application/
│       │   │   │   └── usecase/
│       │   │   │       └── CrearPedidoUseCase.java
│       │   │   ├── infrastructure/
│       │   │   └── PedidosServiceApplication.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
├── docker-compose.yml                   # Infraestructura (Elasticsearch, RabbitMQ)
└── README.md                            # Este archivo
```

## ⚙️ Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- **Java 21 JDK** - [Descargar aquí](https://adoptium.net/)
- **Maven 3.8+** - [Descargar aquí](https://maven.apache.org/download.cgi)
- **Docker y Docker Compose** - [Descargar aquí](https://www.docker.com/get-started)

### Verificar Instalaciones

```bash
# Verificar Java (debe mostrar versión 21)
java -version

# Verificar Maven
mvn -version

# Verificar Docker
docker --version
docker-compose --version
```

## 🐳 Configuración de Infraestructura con Docker

El proyecto incluye un archivo `docker-compose.yml` que levanta automáticamente:
- **Elasticsearch** (puerto 9200)
- **RabbitMQ** (puerto 5672, interfaz web: 15672)

### Crear archivo docker-compose.yml

Crea este archivo en la raíz del proyecto:

```yaml
version: '3.8'

services:
  # Elasticsearch para almacenamiento de datos
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    networks:
      - microservicios-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9200"]
      interval: 30s
      timeout: 10s
      retries: 5

  # RabbitMQ para mensajería entre microservicios
  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: rabbitmq
    ports:
      - "5672:5672"   # Puerto AMQP
      - "15672:15672" # Puerto interfaz web de gestión
    environment:
      - RABBITMQ_DEFAULT_USER=guest
      - RABBITMQ_DEFAULT_PASS=guest
    networks:
      - microservicios-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 30s
      timeout: 10s
      retries: 5

networks:
  microservicios-network:
    driver: bridge
```

### Levantar la Infraestructura

```bash
# Desde la raíz del proyecto
docker-compose up -d

# Verificar que los contenedores están corriendo
docker-compose ps

# Ver logs
docker-compose logs -f
```

### Verificar que todo funciona

- **Elasticsearch**: Abre http://localhost:9200 (deberías ver info del cluster)
- **RabbitMQ Management**: Abre http://localhost:15672 (usuario: guest, password: guest)

## 🏃‍♂️ Ejecutar los Microservicios

### Opción 1: Ejecutar con Maven

```bash
# Terminal 1 - Productos Service
cd productos-service
mvn clean install
mvn spring-boot:run

# Terminal 2 - Pedidos Service
cd pedidos-service
mvn clean install
mvn spring-boot:run
```

### Opción 2: Ejecutar con JAR

```bash
# Compilar ambos microservicios
cd productos-service && mvn clean package && cd ..
cd pedidos-service && mvn clean package && cd ..

# Ejecutar
java -jar productos-service/target/productos-service-1.0.0.jar
java -jar pedidos-service/target/pedidos-service-1.0.0.jar
```

### Verificar que están corriendo

- **Productos Service**: http://localhost:8080/actuator/health
- **Pedidos Service**: http://localhost:8081/actuator/health

## 📚 Documentación de APIs (Swagger)

Una vez que los microservicios estén corriendo, puedes acceder a la documentación interactiva:

### Productos Service
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Pedidos Service
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

## 🔌 Endpoints Principales

### Productos Service (Puerto 8080)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/productos` | Crear un nuevo producto |
| GET | `/api/v1/productos` | Listar todos los productos |
| GET | `/api/v1/productos/{id}` | Obtener producto por ID |
| PUT | `/api/v1/productos/{id}` | Actualizar producto completo |
| PATCH | `/api/v1/productos/{id}/stock` | Actualizar solo el stock |
| DELETE | `/api/v1/productos/{id}` | Eliminar producto |
| GET | `/api/v1/productos/categoria/{categoria}` | Buscar por categoría |
| GET | `/api/v1/productos/buscar?nombre={texto}` | Buscar por nombre |
| GET | `/api/v1/productos/stock-minimo/{cantidad}` | Productos con stock mínimo |

### Pedidos Service (Puerto 8081)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/pedidos` | Crear un nuevo pedido |
| GET | `/api/v1/pedidos` | Listar todos los pedidos |
| GET | `/api/v1/pedidos/{id}` | Obtener pedido por ID |
| GET | `/api/v1/pedidos/cliente/{clienteId}` | Pedidos de un cliente |
| GET | `/api/v1/pedidos/estado/{estado}` | Pedidos por estado |
| PUT | `/api/v1/pedidos/{id}/estado` | Cambiar estado del pedido |
| DELETE | `/api/v1/pedidos/{id}` | Cancelar pedido |

## 🧪 Ejemplos de Uso con cURL

### Crear un Producto

```bash
curl -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Laptop Dell XPS 15",
    "descripcion": "Laptop de alto rendimiento con procesador Intel i7",
    "precio": 1299.99,
    "stock": 50,
    "categoria": "Electrónica"
  }'
```

### Buscar Productos

```bash
# Listar todos
curl http://localhost:8080/api/v1/productos

# Buscar por categoría
curl http://localhost:8080/api/v1/productos/categoria/Electrónica

# Buscar por nombre
curl http://localhost:8080/api/v1/productos/buscar?nombre=laptop
```

### Crear un Pedido

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

## 🧪 Ejecutar Tests

### Tests Unitarios

```bash
# Productos Service
cd productos-service
mvn test

# Pedidos Service
cd pedidos-service
mvn test
```

### Tests de Integración

```bash
# Todos los tests (unitarios + integración)
mvn verify
```

### Ver Reporte de Cobertura

```bash
mvn jacoco:report
# El reporte HTML estará en: target/site/jacoco/index.html
```

## 🔍 Características Avanzadas Implementadas

### Java 21 - Features Modernos

#### Records
```java
// Clases inmutables concisas
public record Producto(
    String id,
    String nombre,
    BigDecimal precio
) {}
```

#### Switch Expressions
```java
return switch (estado) {
    case PENDIENTE -> "En espera";
    case CONFIRMADO -> "Confirmado";
    default -> "Desconocido";
};
```

#### Pattern Matching
```java
if (objeto instanceof String s) {
    return s.toUpperCase();
}
```

### Programación Reactiva con WebFlux

#### Mono y Flux
```java
// Mono: 0 o 1 elemento
Mono<Producto> producto = productoRepository.findById(id);

// Flux: 0 a N elementos
Flux<Producto> productos = productoRepository.findAll();
```

#### Operadores Reactivos
```java
return productoRepository.findAll()
    .filter(p -> p.precio().compareTo(minPrecio) > 0)  // Filtrar
    .map(ProductoDTO::fromDomain)                       // Transformar
    .sort((p1, p2) -> p1.nombre().compareTo(p2.nombre())) // Ordenar
    .collectList();                                     // Agrupar
```

### Streams y Lambdas

```java
// Stream para calcular total
BigDecimal total = items.stream()
    .map(ItemPedido::subtotal)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// Lambda en filter
List<Producto> activos = productos.stream()
    .filter(p -> p.activo())
    .toList();
```

## 📊 Monitoreo y Observabilidad

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas
curl http://localhost:8080/actuator/metrics

# Info de la aplicación
curl http://localhost:8080/actuator/info
```

### RabbitMQ Management

Accede a http://localhost:15672 para:
- Ver colas y exchanges
- Monitorear mensajes
- Ver conexiones activas

### Elasticsearch

```bash
# Ver todos los índices
curl http://localhost:9200/_cat/indices?v

# Ver documentos de productos
curl http://localhost:9200/productos/_search?pretty
```

## 🐛 Solución de Problemas

### Puerto ya en uso

```bash
# Verificar qué proceso usa el puerto
lsof -i :8080
lsof -i :8081

# Matar el proceso
kill -9 PID
```

### Elasticsearch no arranca

```bash
# Ver logs
docker-compose logs elasticsearch

# Reiniciar el contenedor
docker-compose restart elasticsearch
```

### RabbitMQ - Problemas de conexión

```bash
# Verificar estado
docker-compose ps rabbitmq

# Reiniciar
docker-compose restart rabbitmq
```

### Error al compilar - Maven

```bash
# Limpiar y recompilar
mvn clean install -U
```

## 🚀 Despliegue en Producción

### Variables de Entorno

Crea archivos `.env` para cada entorno:

```properties
# application-prod.yml
spring:
  elasticsearch:
    uris: ${ELASTICSEARCH_URL}
  rabbitmq:
    host: ${RABBITMQ_HOST}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
```

### Docker

```bash
# Construir imagen
docker build -t productos-service:1.0.0 ./productos-service
docker build -t pedidos-service:1.0.0 ./pedidos-service

# Ejecutar
docker run -p 8080:8080 productos-service:1.0.0
docker run -p 8081:8081 pedidos-service:1.0.0
```

## 📝 Guía para Subir a Git

### 1. Inicializar repositorio

```bash
cd microservicios-hexagonal
git init
```

### 2. Crear .gitignore

```bash
cat > .gitignore << 'EOF'
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties

# IDE
.idea/
*.iml
.vscode/
.eclipse/
.settings/
.classpath
.project

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db

# Application
application-local.yml
EOF
```

### 3. Primer commit

```bash
git add .
git commit -m "Initial commit: Microservicios con arquitectura hexagonal"
```

### 4. Subir a GitHub

```bash
# Crear repositorio en GitHub primero, luego:
git remote add origin https://github.com/TU_USUARIO/microservicios-hexagonal.git
git branch -M main
git push -u origin main
```

## 👥 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia Apache 2.0 - ver el archivo LICENSE para detalles.

## ✨ Autor

Creado con ❤️ utilizando las mejores prácticas de arquitectura de software y las últimas características de Java 21.

## 📞 Soporte

Si tienes preguntas o problemas:
- Abre un issue en GitHub
- Revisa la documentación de Swagger UI
- Consulta los logs de la aplicación

---

**¡Gracias por usar este proyecto!** 🚀
