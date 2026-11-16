# E-Commerce Microservices Application

A Java and Spring Boot portfolio project exploring an e-commerce backend built from independently running services.

**Status:** In development. The repository contains user, product, and order services, plus service discovery, centralized configuration, and an API gateway. Some operational configuration is machine-specific; see Known limitations.

## Contents

- Architecture
- Services and API
- Technology
- Prerequisites
- Local setup
- Configuration
- Tests
- Repository layout
- Known limitations

## Architecture

![E-Commerce microservices architecture and observability graph](docs/architecture.svg)

The gateway forwards product, user, order, and cart API paths to services registered with Eureka. It configures circuit-breaker fallbacks for the business services. The Config Server reads service YAML files from configserver/src/main/resources/config/.

### Observability

The repository includes an optional observability stack in additional/evaluate-prometheus/docker-compose.yaml:

| Tool | Purpose | Local address |
|---|---|---|
| Prometheus | Scrapes Product, User, and Order metrics from /actuator/prometheus every 3 seconds | http://localhost:9090 |
| Grafana | Dashboards with Prometheus and Loki data sources provisioned by Compose | http://localhost:3000 |
| Loki | Stores and serves logs | http://localhost:3100 |
| Grafana Alloy | Collects Docker container logs and forwards them to Loki | http://localhost:12345 |
| Zipkin | Receives and displays distributed traces | http://localhost:9411 |

The business services and gateway include Micrometer Prometheus and tracing dependencies. Their configuration samples traces at 100%. Prometheus is configured to scrape the three business services, not the gateway. Alloy reads Docker container logs; it does not collect stdout or log files from services launched directly with Maven.

## Services and API

| Component | Port | Persistence | Responsibility |
|---|---:|---|---|
| API Gateway (gateway/) | 8080 | — | Routes client requests and provides fallback responses |
| Product Service (product/) | 8081 | PostgreSQL | Product creation, listing, search, update, and soft deletion |
| User Service (user/) | 8082 | MongoDB | User creation, retrieval, listing, and update |
| Order Service (order/) | 8083 | PostgreSQL | Cart operations and order creation; calls product and user services |
| Eureka (eureka/) | 8761 | — | Service registry |
| Config Server (configserver/) | 8888 | — | Serves externalized service configuration |

Business APIs are available through the gateway at http://localhost:8080 and directly on each service port.

### Product Service

| Method | Path | Behavior |
|---|---|---|
| POST | /api/products | Create a product; returns 201 |
| GET | /api/products | List active products |
| GET | /api/products/{id} | Get an active product; returns 404 if absent |
| GET | /api/products/search?keyword={keyword} | Search products |
| PUT | /api/products/{id} | Update a product; returns 404 if absent |
| DELETE | /api/products/{id} | Soft-delete a product; returns 204 or 404 |

Product creation and update require a nonblank name and category and a positive price. Example:

```bash
curl -i http://localhost:8080/api/products
```

### User Service

| Method | Path | Behavior |
|---|---|---|
| POST | /api/users | Create a user |
| GET | /api/users | List users |
| GET | /api/users/{id} | Get a user; returns 404 if absent |
| PUT | /api/users/{id} | Update a user; returns 404 if absent |

### Order Service

All cart routes require the X-User-ID request header.

| Method | Path | Behavior |
|---|---|---|
| POST | /api/cart | Add an item to the user's cart; returns 201 or 400 |
| GET | /api/cart | Fetch the user's cart |
| DELETE | /api/cart/items/{productId} | Remove an item; returns 204 or 404 |
| POST | /api/orders | Create an order for the user; requires X-User-ID |

### Fallbacks and operations

When a gateway route falls back, the gateway exposes /fallback/products, /fallback/users, or /fallback/orders with a 503 response. Eureka's dashboard is served on port 8761. Actuator endpoints are enabled by service configuration; exact availability depends on the active configuration.

## Technology

- Java 17
- Spring Boot 4.0.7 and Spring Cloud 2025.1.2
- Spring MVC, Spring Data JPA, Spring Data MongoDB
- PostgreSQL for product and order data; MongoDB for user data
- Spring Cloud Gateway, Eureka Discovery, and Config Server
- Resilience4j circuit breakers and retry configuration
- RabbitMQ / Spring Cloud Bus configuration
- Observability: Micrometer, Prometheus, Grafana, Loki, Grafana Alloy, and Zipkin
- Maven, JUnit 5, Mockito, AssertJ

## Prerequisites

- JDK 17
- Maven
- Docker with the Docker Compose plugin
- MongoDB for the user service; the supplied Compose file does not start MongoDB

## Local setup

### 1. Start infrastructure

From the repository root:

```bash
docker compose up -d postgres rabbitmq
```

The Compose file also defines pgAdmin, available at http://localhost:5050 when started with docker compose up -d. Its development defaults are for local use only.

### Optional: Start observability tools

From the repository root, start the separate stack:

```bash
docker compose -f additional/evaluate-prometheus/docker-compose.yaml up -d
```

This starts Prometheus, Grafana, Zipkin, and the Loki logging stack, including Alloy. Open Grafana at http://localhost:3000, Prometheus at http://localhost:9090, and Zipkin at http://localhost:9411. Grafana allows anonymous administrator access in this local stack; do not expose it to an untrusted network. Prometheus scrapes the three application services when they are running and their Actuator metrics endpoints are available. Alloy collects Docker container logs; services run directly with Maven are not Docker containers, so their logs are not collected by this Alloy configuration.

### 2. Configure service properties

The Config Server reads service files from configserver/src/main/resources/config/. Set these environment variables in the shell used to launch the services:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/ecomdb'
export SPRING_DATASOURCE_USERNAME='test'
export SPRING_DATASOURCE_PASSWORD='test'
export SHOW_SQL='false'
export DDL_AUTO='update'
export MONGODB_URL='mongodb://localhost:27017/usersDb'
```

These PostgreSQL values match the supplied Compose defaults. Start a local MongoDB instance separately before starting the User Service.

### 3. Build the services

Each service is a separate Maven project; there is no root Maven aggregator. Order depends on the Product and User artifacts, so install those first:

```bash
mvn -f product/pom.xml clean install
mvn -f user/pom.xml clean install
mvn -f order/pom.xml clean install
mvn -f eureka/pom.xml clean package
mvn -f configserver/pom.xml clean package
mvn -f gateway/pom.xml clean package
```

### 4. Start the application

Run each command in a separate terminal from the repository root. Start infrastructure and MongoDB first, then Eureka and Config Server, followed by business services and the gateway.

```bash
mvn -f eureka/pom.xml spring-boot:run
mvn -f configserver/pom.xml spring-boot:run
mvn -f product/pom.xml spring-boot:run
mvn -f user/pom.xml spring-boot:run
mvn -f order/pom.xml spring-boot:run
mvn -f gateway/pom.xml spring-boot:run
```

Make sure the environment variables from step 2 are available to the relevant service processes. The Config Server currently has an absolute local path in its application.yaml; update it to this checkout's config directory before running elsewhere.

## Configuration

Business services import configuration from http://localhost:8888. Service-specific settings are stored in configserver/src/main/resources/config/.

| Variable | Used by | Purpose |
|---|---|---|
| SPRING_DATASOURCE_URL | Product, Order | PostgreSQL JDBC URL |
| SPRING_DATASOURCE_USERNAME | Product, Order | PostgreSQL username |
| SPRING_DATASOURCE_PASSWORD | Product, Order | PostgreSQL password |
| SHOW_SQL | Product, Order | Whether Hibernate logs SQL |
| DDL_AUTO | Product, Order | Hibernate schema-generation mode |
| MONGODB_URL | User | MongoDB connection URI |

RabbitMQ uses the development guest credentials in service configuration. Keep credentials and production connection strings out of source control.

## Tests

Run a service's tests from the repository root:

```bash
mvn -f product/pom.xml test
mvn -f user/pom.xml test
mvn -f order/pom.xml test
```

Current test sources include service and mapper tests for Product and Order, and mapper tests for User. Controller-level tests are still being added. A focused controller test should use WebMvcTest with MockMvc and a mocked service; use SpringBootTest for tests that need the full application context or real infrastructure.

## Repository layout

```text
ecom-microservice-app/
├── configserver/       # Config Server and per-service configuration
├── eureka/             # Service registry
├── gateway/            # API Gateway and fallback endpoints
├── order/              # Cart and order service
├── product/            # Product catalog service
├── user/               # User service
├── additional/          # Monitoring evaluation resources
├── docker-compose.yml  # Local PostgreSQL, pgAdmin, and RabbitMQ
└── README.md
```

## Known limitations

- The Config Server uses a machine-specific absolute path for its native configuration directory. Update it for your checkout before starting the application elsewhere.
- The supplied Compose file starts PostgreSQL, pgAdmin, and RabbitMQ, but not MongoDB.
- The project is in development; API behavior, service configuration, and test coverage may change.
