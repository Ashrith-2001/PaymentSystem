# High-Level Design (HLD) — Payment System

**Version:** 1.0  
**Date:** 2026-04-16  
**Author:** AI-Assisted Architecture  

---

## 1. System Overview

The Payment System is a **microservices-based backend application** built using Spring Boot 3.x. It models a complete payment processing flow covering user management, product cataloging, order management, payment processing, inventory control, and notifications.

The system follows **event-driven architecture** with **SAGA orchestration** for distributed transaction management across services.

---

## 2. Architecture Style

| Aspect | Choice |
|--------|--------|
| **Architecture Pattern** | Microservices |
| **Internal Service Pattern** | MVC (Model-View-Controller) |
| **Communication (Sync)** | REST APIs via API Gateway |
| **Communication (Async)** | Apache Kafka (Event-Driven) |
| **Transaction Pattern** | SAGA (Orchestration-based) |
| **Service Discovery** | Netflix Eureka |
| **API Gateway** | Spring Cloud Gateway |

---

## 3. System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                   │
│                    (Web / Mobile / Third-party API)                         │
└─────────────────────────────┬───────────────────────────────────────────────┘
                              │ HTTPS / REST
                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           API GATEWAY                                       │
│                    (Spring Cloud Gateway)                                   │
│  ┌──────────┐  ┌──────────────┐  ┌──────────┐  ┌─────────────────────┐    │
│  │ Routing  │  │ JWT Validation│  │  Rate    │  │ Request/Response    │    │
│  │ Engine   │  │ Filter        │  │ Limiting │  │ Logging             │    │
│  └──────────┘  └──────────────┘  └──────────┘  └─────────────────────┘    │
└─────────────────────────────┬───────────────────────────────────────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
         ▼                    ▼                    ▼
┌─────────────┐    ┌──────────────┐    ┌──────────────────┐
│   USER      │    │   PRODUCT    │    │     ORDER        │
│  SERVICE    │    │   SERVICE    │    │    SERVICE       │
│             │    │              │    │  (SAGA Orch.)    │
│ • Register  │    │ • CRUD       │    │ • Create Order   │
│ • Login/JWT │    │ • Categories │    │ • Track Status   │
│ • Profile   │    │ • Search     │    │ • Cancel/Refund  │
│ • RBAC      │    │ • Caching    │    │ • Orchestrate    │
└──────┬──────┘    └──────┬──────┘    └────────┬─────────┘
       │                  │                    │
       ▼                  ▼                    ▼
   [Users DB]        [Products DB]        [Orders DB]
                          │                    │
                          ▼                    │
                     [Caffeine               │
                      Cache]                  │
                                              │
         ┌────────────────┴────────────────────┐
         │         KAFKA EVENT BUS             │
         │                                      │
         │  Topics:                             │
         │  • order-events                      │
         │  • payment-events                    │
         │  • inventory-events                  │
         │  • notification-events               │
         └──────┬──────────────┬───────────────┘
                │              │
                ▼              ▼
     ┌──────────────┐  ┌──────────────────┐
     │   PAYMENT    │  │   INVENTORY      │
     │   SERVICE    │  │   SERVICE        │
     │              │  │                  │
     │ • Process    │  │ • Stock Mgmt     │
     │ • Refund     │  │ • Reservation    │
     │ • Strategies │  │ • Pessimistic    │
     │ • Idempotent │  │   Locking        │
     └──────┬──────┘  └──────┬───────────┘
            │                │
            ▼                ▼
       [Payments DB]    [Inventory DB]
            │                │
            └────────┬───────┘
                     ▼
         ┌──────────────────┐
         │  NOTIFICATION    │
         │   SERVICE        │
         │                  │
         │ • Email (mock)   │
         │ • SMS (mock)     │
         │ • Event Logging  │
         └──────────────────┘

         ┌──────────────────┐
         │ SERVICE REGISTRY │
         │ (Eureka Server)  │
         │                  │
         │ All services     │
         │ register here    │
         └──────────────────┘
```

---

## 4. Service Decomposition

### 4.1 User Service (Port: 8081)
**Responsibility:** User lifecycle management and authentication.

| Capability | Details |
|-----------|---------|
| Registration | Email-based registration with BCrypt password hashing |
| Authentication | JWT token generation and validation |
| Authorization | Role-Based Access Control (ADMIN, CUSTOMER) |
| Profile Management | CRUD operations on user profiles |
| Address Management | Multiple addresses per user |

**Database Tables:** `users`, `addresses`

---

### 4.2 Product Service (Port: 8082)
**Responsibility:** Product catalog and category management.

| Capability | Details |
|-----------|---------|
| Product CRUD | Create, read, update, delete products |
| Category Management | Hierarchical category system |
| Search & Filter | By name, category, price range with pagination |
| Caching | Caffeine in-memory cache for frequently accessed products |

**Database Tables:** `products`, `categories`

---

### 4.3 Order Service — SAGA Orchestrator (Port: 8083)
**Responsibility:** Order lifecycle and distributed transaction orchestration.

| Capability | Details |
|-----------|---------|
| Order Creation | Validates and creates orders with line items |
| SAGA Orchestration | Coordinates Inventory → Payment → Confirmation flow |
| Compensating Txns | Rolls back on failure (release stock, refund payment) |
| Status Tracking | Full order status history with audit trail |
| Optimistic Locking | `@Version` on Order entity to prevent lost updates |

**Database Tables:** `orders`, `order_items`, `order_status_history`

**SAGA Steps (Happy Path):**
1. Create Order → status: `PENDING`
2. Reserve Inventory → status: `INVENTORY_RESERVED`
3. Process Payment → status: `PAYMENT_COMPLETED`
4. Confirm Order → status: `CONFIRMED`

**SAGA Steps (Compensation on Payment Failure):**
1. ~~Process Payment~~ → FAILED
2. Release Reserved Inventory (compensate step 2)
3. Mark Order as `PAYMENT_FAILED`

---

### 4.4 Payment Service (Port: 8084)
**Responsibility:** Payment processing with multiple payment method support.

| Capability | Details |
|-----------|---------|
| Payment Processing | Strategy pattern for CREDIT_CARD, DEBIT_CARD, WALLET, NET_BANKING |
| Idempotency | Idempotency key per payment request to prevent double-charging |
| Refunds | Full and partial refund support |
| Transaction Log | Complete transaction audit trail |
| Pessimistic Locking | Prevents concurrent payment on same order |

**Database Tables:** `payments`, `transactions`, `refunds`

**Design Patterns:**
- **Strategy Pattern:** `PaymentStrategy` interface → `CreditCardPayment`, `WalletPayment`, etc.
- **Factory Pattern:** `PaymentStrategyFactory` resolves strategy at runtime

---

### 4.5 Inventory Service (Port: 8085)
**Responsibility:** Stock management and reservation.

| Capability | Details |
|-----------|---------|
| Stock Management | Track available quantity per product |
| Reservation | Temporary stock holds during order processing |
| Release | Release reserved stock on order cancellation/failure |
| Concurrency Safety | Pessimistic locking (`SELECT FOR UPDATE`) on stock updates |

**Database Tables:** `inventory`, `stock_reservations`

---

### 4.6 Notification Service (Port: 8086)
**Responsibility:** Event-driven notifications.

| Capability | Details |
|-----------|---------|
| Event Listening | Kafka consumer for order/payment events |
| Multi-Channel | Strategy pattern for EMAIL, SMS dispatch (mocked) |
| Templating | Template-based message formatting |
| Logging | All notifications logged for audit |

**Database Tables:** `notification_logs`

---

### 4.7 API Gateway (Port: 8080)
**Responsibility:** Single entry point, cross-cutting concerns.

| Capability | Details |
|-----------|---------|
| Routing | Route to downstream services based on path prefix |
| Authentication | JWT token validation before forwarding |
| Rate Limiting | Request throttling per client |
| Logging | Request/Response logging |
| CORS | Cross-origin resource sharing configuration |

---

### 4.8 Service Registry — Eureka (Port: 8761)
**Responsibility:** Service discovery and health monitoring.

---

## 5. Data Flow — Order Processing (SAGA)

```
Client                 API GW    Order Svc    Kafka    Inventory    Payment    Notification
  │                      │          │           │          │           │           │
  │──POST /orders───────>│          │           │          │           │           │
  │                      │──route──>│           │          │           │           │
  │                      │          │──create───│          │           │           │
  │                      │          │  (PENDING)│          │           │           │
  │                      │          │──publish──>           │           │           │
  │                      │          │  OrderCreated         │           │           │
  │                      │<─202────│           │          │           │           │
  │<─────202 Accepted────│          │           │          │           │           │
  │                      │          │           │──consume─>│           │           │
  │                      │          │           │          │──reserve──│           │
  │                      │          │           │          │  stock    │           │
  │                      │          │           │<─publish──│           │           │
  │                      │          │           │  InventoryReserved    │           │
  │                      │          │           │──────────────────────>│           │
  │                      │          │           │          │           │──process──│
  │                      │          │           │          │           │  payment  │
  │                      │          │           │<─────────────────────│           │
  │                      │          │           │  PaymentCompleted     │           │
  │                      │          │<─consume──│          │           │           │
  │                      │          │──confirm──│          │           │           │
  │                      │          │(CONFIRMED)│          │           │           │
  │                      │          │──publish──>           │           │           │
  │                      │          │  OrderConfirmed       │           │           │
  │                      │          │           │──────────────────────────────────>│
  │                      │          │           │          │           │  send     │
  │                      │          │           │          │           │  notify   │
```

---

## 6. Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Language | Java 21+ | Application development |
| Framework | Spring Boot 3.3.x | Application framework |
| Cloud | Spring Cloud 2023.0.x | Gateway, Eureka, Config |
| Data Access | Spring Data JPA / Hibernate | ORM & database operations |
| Database (Dev) | H2 (PostgreSQL mode) | In-memory relational database |
| Database (Prod) | PostgreSQL | Production database |
| Caching | Caffeine | In-memory caching |
| Messaging | Apache Kafka (Embedded) | Async event communication |
| Security | Spring Security + JWT | Auth & authorization |
| Docs | SpringDoc OpenAPI 2.x | Swagger API documentation |
| Build | Maven + Maven Wrapper | Build automation |
| Utilities | Lombok, MapStruct | Boilerplate reduction |
| Monitoring | Spring Actuator | Health checks & metrics |
| Logging | SLF4J + Logback | Structured logging |

---

## 7. Design Patterns Used

### Microservice Patterns
| Pattern | Implementation | Service |
|---------|---------------|---------|
| **SAGA (Orchestration)** | `OrderSagaOrchestrator` coordinates distributed txn | Order Service |
| **Event-Driven Architecture** | Kafka producers/consumers for async communication | All services |
| **API Gateway** | Spring Cloud Gateway with filters | API Gateway |
| **Service Discovery** | Eureka Server/Client | Service Registry |

### GoF Design Patterns
| Pattern | Implementation | Service |
|---------|---------------|---------|
| **Strategy** | `PaymentStrategy`, `NotificationStrategy` | Payment, Notification |
| **Factory** | `PaymentStrategyFactory` | Payment |
| **Observer** | Order status change listeners | Order |
| **Adapter** | Payment gateway adapter interface | Payment |

---

## 8. Security Architecture

```
┌───────────┐     ┌────────────┐     ┌──────────────┐
│  Client   │────>│ API Gateway │────>│  User Service │
│           │     │             │     │  (JWT Issue)  │
│           │     │ JWT Filter  │     └──────────────┘
│           │     │ validates   │
│           │     │ all requests│────>│ Other Services│
└───────────┘     └────────────┘     │ (Protected)   │
                                     └───────────────┘
```

- **Login Flow:** Client → API Gateway → User Service → Returns JWT
- **Authenticated Flow:** Client sends JWT in `Authorization: Bearer <token>` → Gateway validates → Forwards to service
- **RBAC:** `ADMIN` can manage products/inventory; `CUSTOMER` can browse and order

---

## 9. Concurrency & Locking Strategy

| Scenario | Strategy | Mechanism |
|----------|----------|-----------|
| Order update by multiple users | Optimistic Locking | `@Version` column, retry on `OptimisticLockException` |
| Stock reservation | Pessimistic Locking | `@Lock(PESSIMISTIC_WRITE)` / `SELECT FOR UPDATE` |
| Payment processing | Pessimistic Locking + Idempotency | DB lock + idempotency key check |
| Async event processing | Single consumer per partition | Kafka consumer group |

---

## 10. Error Handling Strategy

```
┌─────────────────────────────────────────────┐
│              Global Exception Handler       │
│          (@RestControllerAdvice)             │
├─────────────────────────────────────────────┤
│                                             │
│  ResourceNotFoundException → 404            │
│  BusinessException → 400                    │
│  InsufficientFundsException → 402           │
│  InsufficientStockException → 409           │
│  InvalidCredentialsException → 401          │
│  AccessDeniedException → 403                │
│  MethodArgumentNotValid → 400               │
│  OptimisticLockException → 409 (retry)      │
│  Exception (catch-all) → 500                │
│                                             │
│  Response Format:                           │
│  {                                          │
│    "success": false,                        │
│    "message": "Human-readable error",       │
│    "errorCode": "ERR_INSUFFICIENT_STOCK",   │
│    "timestamp": "2026-04-16T18:00:00Z",     │
│    "path": "/api/orders"                    │
│  }                                          │
└─────────────────────────────────────────────┘
```

---

## 11. Port Allocation

| Service | Port | Context Path |
|---------|------|-------------|
| API Gateway | 8080 | `/` |
| User Service | 8081 | `/api/users`, `/api/auth` |
| Product Service | 8082 | `/api/products`, `/api/categories` |
| Order Service | 8083 | `/api/orders` |
| Payment Service | 8084 | `/api/payments` |
| Inventory Service | 8085 | `/api/inventory` |
| Notification Service | 8086 | `/api/notifications` |
| Eureka Server | 8761 | `/` |

---

## 12. Non-Functional Requirements

| Requirement | Approach |
|------------|---------|
| **Scalability** | Stateless services, horizontal scaling ready |
| **Reliability** | SAGA compensations, idempotency, retry mechanisms |
| **Observability** | Structured logging (JSON), Spring Actuator, MDC tracing |
| **Graceful Shutdown** | `server.shutdown=graceful` with 30s timeout |
| **Configuration** | Externalized via `application.yml` with profiles |
| **API Documentation** | Auto-generated Swagger UI per service |
