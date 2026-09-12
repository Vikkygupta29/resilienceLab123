# ResilienceLab

## Microservices Resilience & Failure-Handling Platform

ResilienceLab is an internal developer-focused platform for demonstrating, testing, and observing how microservices behave under failures and degraded conditions.

The project focuses on **microservices resilience, controlled fault injection, asynchronous messaging, observability, and failure recovery**.

> ResilienceLab is not an e-commerce application. The order flow is used as a simple distributed workflow to demonstrate resilience and failure-handling concepts.

---

## 🚀 Project Overview

ResilienceLab contains multiple Spring Boot microservices connected through synchronous REST communication and asynchronous Kafka messaging.

The platform allows developers to:

* Simulate service failures
* Introduce artificial latency
* Test retries and circuit breakers
* Test timeout handling
* Apply bulkhead isolation
* Apply rate limiting
* Test Kafka retry and Dead Letter Topics
* Prevent duplicate Kafka event processing
* Use the Transactional Outbox pattern
* Inject Redis-related failures
* Cache inventory data using Redis
* Monitor service health and metrics
* Monitor Kafka consumer lag
* Visualize metrics through Grafana
* Trace distributed requests using Jaeger
* View operational information through a React dashboard
* Run the complete backend and infrastructure using Docker Compose

---

# 🏗️ Architecture

```text
                         ┌──────────────────────┐
                         │   React Dashboard    │
                         │      Frontend        │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     API Gateway      │
                         │       :8080          │
                         │                      │
                         │ • Routing            │
                         │ • Rate Limiting      │
                         │ • Correlation ID     │
                         └──────────┬───────────┘
                                    │
                  ┌─────────────────┼─────────────────┐
                  │                 │                 │
                  ▼                 ▼                 ▼
        ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
        │  Order Service  │ │Inventory Service│ │ Payment Service │
        │      :8081      │ │      :8083      │ │      :8082      │
        └────────┬────────┘ └────────┬────────┘ └────────┬────────┘
                 │                   │                   │
                 └───────────────────┼───────────────────┘
                                     │
                              ┌──────▼──────┐
                              │    Kafka    │
                              │    :9092    │
                              └─────────────┘

        ┌─────────────┐     ┌─────────────┐
        │    MySQL    │     │    Redis    │
        │    :3306    │     │    :6379    │
        └─────────────┘     └─────────────┘

        ┌─────────────┐     ┌─────────────┐
        │ Prometheus  │────▶│   Grafana   │
        │    :9090    │     │    :3000    │
        └─────────────┘     └─────────────┘

                         ┌─────────────┐
                         │   Jaeger    │
                         │   :16686    │
                         └─────────────┘
```

---

# 🧩 Services

| Service           |    Port | Responsibility                             |
| ----------------- | ------: | ------------------------------------------ |
| API Gateway       |  `8080` | Routing, rate limiting, correlation ID     |
| Order Service     |  `8081` | Order processing and orchestration         |
| Payment Service   |  `8082` | Payment processing                         |
| Inventory Service |  `8083` | Inventory reservation                      |
| MySQL             |  `3306` | Persistent data storage                    |
| Redis             |  `6379` | Caching, rate limiting and fault injection |
| Kafka             |  `9092` | Event-driven communication                 |
| Prometheus        |  `9090` | Metrics collection                         |
| Grafana           |  `3000` | Metrics visualization                      |
| Jaeger            | `16686` | Distributed tracing                        |

---

# 🛠️ Technology Stack

### Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* MySQL
* Spring Kafka
* Resilience4j
* Redis
* Micrometer
* Spring Boot Actuator
* OpenTelemetry

### Messaging

* Apache Kafka
* Kafka Retry Topics
* Kafka Dead Letter Topics
* Transactional Outbox
* Event-based communication
* Idempotent event processing

### Observability

* Prometheus
* Grafana
* OpenTelemetry
* Jaeger
* Structured JSON logging
* MDC
* Correlation IDs

### Frontend

* React
* JavaScript
* Tailwind CSS
* Recharts
* Lucide React

### Infrastructure

* Docker
* Docker Compose

---

# 🔄 Order Processing Flow

ResilienceLab supports both synchronous and asynchronous communication.

## Synchronous Flow

The synchronous baseline uses Spring `RestClient`.

```text
Client
   │
   ▼
Order Service
   │
   ▼
Inventory Service
   │
   ▼
Order Service
   │
   ▼
Payment Service
   │
   ▼
Order Service
   │
   ▼
CONFIRMED
```

The caller waits for the response during synchronous communication.

---

# 📨 Kafka Event-Driven Flow

The asynchronous workflow uses Kafka.

```text
Order Service
     │
     │ order.created
     ▼
Inventory Service
     │
     │ inventory.reserved
     ▼
Order Service
     │
     │ payment.requested
     ▼
Payment Service
     │
     │ payment.completed
     ▼
Order Service
     │
     ▼
CONFIRMED
```

---

# 📡 Kafka Topics

The project currently uses the following main topics:

| Topic                | Purpose                          |
| -------------------- | -------------------------------- |
| `order.created`      | Order creation event             |
| `inventory.reserved` | Successful inventory reservation |
| `payment.requested`  | Payment processing request       |
| `payment.completed`  | Successful payment completion    |
| `inventory.failed`   | Inventory processing failure     |
| `payment.failed`     | Payment processing failure       |

Retry and DLT topics are generated for failed Kafka processing.

Examples:

```text
order.created-retry
order.created-dlt

payment.requested-retry
payment.requested-dlt
```

---

# 🛡️ Resilience4j

Resilience4j is used to protect synchronous service communication and demonstrate different failure-handling strategies.

## Retry

Automatically retries failed service calls.

Example:

```text
Attempt 1 → Failure
Attempt 2 → Success
```

---

## Circuit Breaker

Prevents continuous calls to an unhealthy service.

```text
CLOSED
   │
   │ failures
   ▼
OPEN
   │
   │ recovery period
   ▼
HALF_OPEN
   │
   ▼
CLOSED
```

When a service continues failing, the circuit opens and prevents additional calls.

---

## Timeout

Prevents a service call from waiting indefinitely.

Example:

```text
Order Service
      │
      │ request
      ▼
Inventory Service
      │
      │ delayed response
      ▼
Timeout
```

---

## Fallback

Provides controlled behavior when a protected service call cannot be completed normally.

---

## Bulkhead

Limits concurrent operations to prevent one failing or overloaded dependency from consuming all available resources.

---

## Rate Limiter

Controls the number of requests allowed within a configured time period.

---

## Exponential Backoff

Retries can use increasing delays between attempts to avoid repeatedly hitting an unhealthy dependency.

---

## Combined Retry + Circuit Breaker

The project demonstrates combining resilience mechanisms so that:

```text
Request
   │
   ▼
Retry
   │
   ├── Success → Continue
   │
   └── Repeated Failure
             │
             ▼
       Circuit Breaker
             │
             ▼
           OPEN
```

---

# 📨 Kafka Retry & Dead Letter Topic

Kafka consumers use retry handling for failed event processing.

Example:

```text
order.created
      │
      ▼
Attempt 1
      │
   Failure
      ▼
Retry Topic
      │
      ▼
Attempt 2
      │
   Failure
      ▼
Retry Topic
      │
      ▼
Attempt 3
      │
   Failure
      ▼
Dead Letter Topic
```

After all retry attempts are exhausted, the event is moved to the appropriate DLT.

The application can then publish a failure event such as:

```text
inventory.failed
```

or:

```text
payment.failed
```

This allows the Order Service to update the order status appropriately.

---

# 🔁 Kafka Idempotency

Kafka consumers include duplicate-event protection.

This prevents the same event from being processed multiple times when duplicate delivery occurs.

Conceptually:

```text
Event
  │
  ▼
Check event ID
  │
  ├── Already processed → Ignore
  │
  └── New event → Process
```

This helps maintain consistent order, inventory, and payment state.

---

# 📦 Transactional Outbox

The Transactional Outbox pattern is implemented to reliably publish events.

Instead of directly depending on Kafka availability during a database transaction:

```text
Database Transaction
       │
       ├── Save business data
       │
       └── Save Outbox Event
```

A publisher later processes pending outbox events:

```text
Outbox Table
     │
     ▼
Outbox Publisher
     │
     ▼
Kafka
```

This reduces the risk of losing an event when database and Kafka operations do not complete together.

---

# 🔴 Redis

Redis is used for multiple resilience-related purposes.

## Redis Rate Limiting

The API Gateway uses Redis together with Bucket4j for distributed request-rate control.

```text
Client
  │
  ▼
API Gateway
  │
  ▼
Redis Rate Limit
  │
  ├── Allowed → Service
  │
  └── Limit exceeded → 429
```

---

# 🗄️ Inventory Caching

Inventory information is cached using Redis.

```text
Request
   │
   ▼
Redis Cache
   │
   ├── Cache Hit → Return data
   │
   └── Cache Miss
          │
          ▼
     Database
```

Inventory cache eviction is also implemented to remove stale cached information when required.

---

# 💥 Redis Fault Injection

ResilienceLab includes controlled Redis fault injection.

Supported fault modes include:

```text
NORMAL
LATENCY
FAIL
TIMEOUT
RATE_LIMITED
```

These modes allow developers to test how services behave when Redis becomes slow, unavailable, or rate-limited.

Fault injection is controlled through the application's fault-injection mechanism.

---

# 🚪 API Gateway

The API Gateway is implemented using Spring Cloud Gateway.

It provides:

* Order service routing
* Inventory service routing
* Payment service routing
* Redis-backed rate limiting
* Bucket4j rate limiting
* Correlation ID propagation
* Centralized entry point for backend APIs

Example:

```text
Client
  │
  ▼
Gateway :8080
  │
  ├── /service/order     → Order Service :8081
  │
  ├── /service/inventory → Inventory Service :8083
  │
  └── /service/payment   → Payment Service :8082
```

---

# 🆔 Correlation ID

Requests are assigned a correlation ID so that the same request can be tracked across services.

Example:

```text
Client
  │
  │ correlation ID
  ▼
API Gateway
  │
  ▼
Order Service
  │
  ▼
Inventory Service
  │
  ▼
Payment Service
```

The correlation ID is also included in structured logs where available.

This makes distributed troubleshooting easier.

---

# 📊 Observability

ResilienceLab includes a complete observability stack.

```text
Applications
     │
     ├──────────────► Prometheus
     │                    │
     │                    ▼
     │                 Grafana
     │
     └──────────────► Jaeger
```

---

# ❤️ Health Monitoring

Spring Boot Actuator exposes health and operational endpoints.

The dashboard uses service health information to determine whether services are available.

The services expose Actuator metrics and Prometheus-compatible metrics.

---

# 📈 Prometheus

Prometheus collects application metrics from the Spring Boot services.

Metrics include:

* HTTP request metrics
* Request counts
* Error rates
* JVM metrics
* Database metrics
* Resilience4j metrics
* Application metrics
* Custom resilience metrics

---

# 📉 Grafana

Grafana is used to visualize ResilienceLab metrics.

The dashboard contains panels for areas including:

* Request rate
* P95 request latency
* 5xx / 429 error rate
* Circuit breaker state
* Kafka consumer lag
* Order status
* Service health
* Resilience-related metrics

Grafana makes it possible to observe the effect of injected failures in real time.

---

# 🔭 Distributed Tracing

OpenTelemetry and Jaeger are used for distributed tracing.

A single order can be traced across:

```text
Order Service
      │
      ▼
Kafka
      │
      ▼
Inventory Service
      │
      ▼
Order Service
      │
      ▼
Kafka
      │
      ▼
Payment Service
      │
      ▼
Order Service
```

A complete trace can contain spans such as:

```text
order-service
    │
    ├── order.created send
    │
    ▼
inventory-service
    │
    ├── order.created process
    ├── inventory.reserved send
    │
    ▼
order-service
    │
    ├── inventory.reserved process
    ├── payment.requested send
    │
    ▼
payment-service
    │
    ├── payment.requested process
    ├── payment.completed send
    │
    ▼
order-service
    │
    └── payment.completed process
```

Jaeger provides a visual representation of this distributed workflow.

---

# 📝 Structured Logging

The services use structured JSON logging.

Log information can include:

* Timestamp
* Service name
* Log level
* Order ID
* Event ID
* Correlation ID
* Message
* Exception information

Example structure:

```json
{
  "service": "order-service",
  "level": "INFO",
  "orderId": "example-order-id",
  "eventId": "example-event-id",
  "correlationId": "example-correlation-id",
  "message": "Payment completed"
}
```

Structured logging makes logs easier to search, analyze, and correlate across distributed services.

---

# 📊 Custom Metrics

The project includes application-level metrics for important operational events.

Examples include:

```text
orders_confirmed_total
orders_pending_total
faults_injected_total
```

These metrics can be consumed by Prometheus and visualized in Grafana.

---

# 🖥️ React Operations Dashboard

ResilienceLab includes a React-based Operations Dashboard.

The dashboard provides a centralized view of the platform.

## Dashboard Areas

### Overview

Displays information such as:

* Services Online
* Orders Confirmed
* Faults Injected
* Error Rate
* Request Rate
* P95 Request Latency
* 5xx / 429 Errors
* Circuit Breaker State
* Kafka Consumer Lag
* Service Health

### Metrics

Provides application and infrastructure metrics.

### Kafka

Provides Kafka-related operational information such as consumer lag.

### Resilience

Displays resilience-related information including circuit breaker state.

### Fault Injection

Provides a UI for controlled fault-injection operations.

### Settings

Provides dashboard/application settings.

---

# 💥 Failure Demonstration

One of the main purposes of ResilienceLab is to demonstrate controlled failures.

A typical demonstration is:

```text
1. Create an order
        │
        ▼
2. Order requests inventory
        │
        ▼
3. Inject Inventory latency
        │
        ▼
4. Inventory becomes slow
        │
        ▼
5. Order Service retries
        │
        ▼
6. Circuit Breaker eventually opens
        │
        ▼
7. Order is not immediately confirmed
        │
        ▼
8. Metrics change
        │
        ├── Request latency increases
        ├── Error/pending metrics change
        └── Circuit state changes
        │
        ▼
9. Observe complete trace in Jaeger
        │
        ▼
10. Remove the injected fault
        │
        ▼
11. Safely retry/recover processing
```

This demonstrates how resilience mechanisms and observability work together.

---

# 🐳 Docker Compose

The project includes Docker Compose configuration for running the backend services and supporting infrastructure together.

Docker Compose currently includes:

```text
MySQL
Redis
Kafka
Prometheus
Grafana
Jaeger
Order Service
Inventory Service
Payment Service
API Gateway
```

The React frontend is intentionally run separately.

---

# ▶️ Running the Project

## Prerequisites

Install:

* Java 21
* Maven
* Node.js
* npm
* Docker Desktop
* Git

---

## Start Backend and Infrastructure

From the infrastructure directory:

```bash
docker compose up -d
```

To rebuild images after backend changes:

```bash
docker compose build
docker compose up -d
```

To rebuild only one changed service:

```bash
docker compose build inventory-service
docker compose up -d inventory-service
```

---

# 🔍 Check Running Containers

```bash
docker compose ps
```

You should see the ResilienceLab containers running.

---

# 🩺 Health Checks

Order Service:

```text
http://localhost:8081/actuator/health
```

Inventory Service:

```text
http://localhost:8083/actuator/health
```

Payment Service:

```text
http://localhost:8082/actuator/health
```

API Gateway:

```text
http://localhost:8080
```

---

# 📊 Monitoring URLs

Prometheus:

```text
http://localhost:9090
```

Grafana:

```text
http://localhost:3000
```

Jaeger:

```text
http://localhost:16686
```

---

# 🌐 Frontend

The React dashboard runs separately from Docker Compose.

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The Vite development server will provide the frontend URL shown in the terminal.

---

# 🧪 Example Order Request

An order can be created through the API Gateway.

Example:

```http
POST http://localhost:8080/service/order/api/orders
Content-Type: application/json
```

Example request body:

```json
{
  "productId": "keyboard-1",
  "quantity": 10,
  "amount": 2998
}
```

A successful order can eventually reach:

```text
CONFIRMED
```

depending on the currently configured system state and injected faults.

---

# 🗃️ Database

MySQL is used as the persistent database.

The project contains separate databases for the services, including:

```text
order_db
inventory_db
payment_db
```

The database stores business state and supports the Transactional Outbox implementation.

---

# 🔄 ResilienceLab Failure Model

The platform demonstrates several categories of failures.

## Service Failure

```text
Service unavailable
       │
       ▼
Retry
       │
       ▼
Circuit Breaker
       │
       ▼
Fallback / failure handling
```

## Latency

```text
Normal Request
      │
      ▼
Injected Delay
      │
      ▼
Timeout / Retry
```

## Kafka Failure

```text
Consumer
   │
   ▼
Failure
   │
   ▼
Retry Topic
   │
   ▼
Repeated Failure
   │
   ▼
DLT
```

## Redis Failure

```text
Redis
 │
 ├── Normal
 ├── Latency
 ├── Failure
 ├── Timeout
 └── Rate Limited
```

---

# 📁 Project Structure

A simplified project structure is:

```text
ResilienceLab/
│
├── api-gateway/
│
├── order-service/
│
├── inventory-service/
│
├── payment-service/
│
├── frontend/
│
├── infrastructure/
│   ├── docker-compose.yml
│   ├── prometheus/
│   └── grafana-data/
│
└── README.md
```

---

# 🔐 Security Note

The current ResilienceLab implementation does **not** include JWT authentication or JWT authorization.

The API Gateway currently focuses on:

* Request routing
* Redis/Bucket4j rate limiting
* Correlation ID propagation
* Centralized API entry

JWT should only be documented here after it is actually implemented.

---

# 🎯 Main Learning Objectives

ResilienceLab demonstrates how to design and operate resilient microservices using:

* Synchronous REST communication
* Asynchronous Kafka communication
* Retry
* Circuit Breaker
* Timeout
* Fallback
* Bulkhead
* Rate Limiter
* Exponential Backoff
* Kafka Retry
* Kafka DLT
* Kafka Idempotency
* Transactional Outbox
* Redis caching
* Redis fault injection
* API Gateway
* Correlation IDs
* Structured logging
* Prometheus
* Grafana
* OpenTelemetry
* Jaeger
* Docker Compose
* React operational dashboards

---

# 🧠 Design Principles

The project follows several important distributed-system principles.

### Fail Fast

Do not allow unhealthy dependencies to consume resources indefinitely.

### Retry Carefully

Retries should be controlled and combined with appropriate backoff.

### Stop Repeated Failures

Circuit breakers prevent continuously calling unhealthy services.

### Isolate Resources

Bulkheads prevent one dependency from exhausting shared resources.

### Handle Asynchronous Failures

Kafka retry and DLT mechanisms prevent failed messages from disappearing silently.

### Prevent Duplicate Processing

Idempotency protects consumers from duplicate event delivery.

### Maintain Event Reliability

Transactional Outbox helps ensure events are not lost between database operations and message publishing.

### Observe Everything

Metrics, logs, traces, and health information make distributed failures easier to understand.

---

# 📈 Operational Flow

The overall platform can be viewed as:

```text
                 ┌─────────────────────┐
                 │       Client        │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │    API Gateway      │
                 │ Routing + Rate      │
                 │ Limiting + CID      │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │   Order Service     │
                 └──────┬───────┬──────┘
                        │       │
              REST      │       │ Kafka
                        │       │
                        ▼       ▼
                 ┌──────────┐  ┌──────────┐
                 │Inventory │  │  Kafka   │
                 │ Service  │  │          │
                 └──────────┘  └────┬─────┘
                                    │
                                    ▼
                              ┌──────────┐
                              │ Payment  │
                              │ Service  │
                              └──────────┘


        ┌──────────┐       ┌──────────┐
        │Prometheus│──────▶│ Grafana  │
        └──────────┘       └──────────┘

        ┌──────────┐
        │ Services │──────▶ Jaeger
        │ + Kafka  │
        └──────────┘

        ┌──────────┐
        │  Redis   │
        │ Cache +  │
        │ Faults   │
        └──────────┘
```

---

# 🧪 Testing Strategy

The platform can be tested by intentionally introducing failures and observing the resulting behavior.

Examples:

### Inventory Latency

```text
Inject latency
     ↓
Inventory slows down
     ↓
Order request latency increases
     ↓
Retry / timeout behavior
     ↓
Observe metrics
     ↓
Observe trace
```

### Inventory Failure

```text
Inject failure
     ↓
Inventory request fails
     ↓
Retry attempts
     ↓
Circuit Breaker behavior
     ↓
Failure handling
```

### Kafka Consumer Failure

```text
Consumer failure
     ↓
Retry
     ↓
Retry
     ↓
Retry
     ↓
DLT
     ↓
Failure event
     ↓
Order status updated
```

---

# 🛠️ Git Workflow

Development is organized using feature branches.

Example:

```bash
git checkout -b feature/my-feature
```

After making and testing changes:

```bash
git add .
git commit -m "feat: add my feature"
git push origin feature/my-feature
```

The project uses small, focused commits so that individual resilience features can be tracked independently.

---

# 📌 Current Project Status

The following major components have been implemented:

* [x] Microservices
* [x] MySQL persistence
* [x] Synchronous RestClient communication
* [x] Resilience4j Retry
* [x] Circuit Breaker
* [x] Fallback
* [x] Timeout
* [x] Bulkhead
* [x] Rate Limiter
* [x] Exponential Backoff
* [x] Combined Retry + Circuit Breaker
* [x] Kafka event-driven communication
* [x] Kafka retry
* [x] Kafka Dead Letter Topics
* [x] Kafka idempotency
* [x] Transactional Outbox
* [x] Redis
* [x] Redis fault injection
* [x] Redis inventory caching
* [x] Inventory cache eviction
* [x] API Gateway
* [x] Gateway routing
* [x] Gateway rate limiting
* [x] Correlation ID
* [x] Actuator
* [x] Prometheus metrics
* [x] Grafana dashboards
* [x] Kafka consumer lag monitoring
* [x] Distributed tracing
* [x] OpenTelemetry
* [x] Jaeger
* [x] Structured JSON logging
* [x] React Operations Dashboard
* [x] Docker Compose
* [x] Dockerized backend services
* [ ] JWT authentication/authorization
* [ ] Kubernetes deployment
* [ ] Production cloud deployment

---

# 🚀 Future Improvements

Potential future improvements include:

* JWT authentication and authorization
* Automated integration tests
* Load testing
* CI/CD pipeline
* Production deployment
* Advanced alerting
* Additional fault-injection scenarios
* Automated resilience testing

---

# 👨‍💻 Project Purpose

ResilienceLab was built as a practical demonstration of how modern distributed systems can remain observable and recoverable when individual components fail.

The project combines:

```text
Microservices
      +
Resilience Patterns
      +
Kafka
      +
Redis
      +
Observability
      +
Distributed Tracing
      +
Operational Dashboard
      +
Docker
```

The result is a controlled environment for understanding **how failures propagate through distributed systems and how resilience mechanisms can detect, contain, and recover from those failures.**

---

## 📄 License

This project is intended for educational, demonstration, and internal development purposes.
