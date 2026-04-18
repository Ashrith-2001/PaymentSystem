# Payment System — Microservices Architecture

A **production-grade, microservices-oriented payment system** built with Spring Boot 3.x. Implements event-driven architecture with SAGA orchestration for distributed transaction management.

## 🏗️ Architecture

```
Client → API Gateway (8080) → Microservices (808x) → Kafka Event Bus
```

| Service | Port | Description |
|---------|------|-------------|
| **API Gateway** | 8080 | Single entry point, JWT validation, routing |
| **User Service** | 8081 | Registration, JWT auth, RBAC |
| **Product Service** | 8082 | Product catalog, categories, caching |
| **Order Service** | 8083 | Order management, SAGA orchestrator |
| **Payment Service** | 8084 | Payment processing (Strategy pattern) |
| **Inventory Service** | 8085 | Stock management, pessimistic locking |
| **Notification Service** | 8086 | Event-driven notifications |
| **Service Registry** | 8761 | Eureka service discovery |

## 📋 Prerequisites

- **Java 21+** installed (`java -version` to verify)
- **Git** installed (optional, for version control)
- No other installations required (uses embedded DB, cache, and Maven Wrapper)

## 🚀 Getting Started

### 1. Install Maven Wrapper (one-time setup)

If you don't have Maven installed globally, download the Maven Wrapper:

```bash
# The Maven Wrapper (mvnw) is included in the project
# If it's missing, you can generate it with:
mvn wrapper:wrapper
```

### 2. Build the Project

```bash
# Build all modules
./mvnw clean compile        # Linux/Mac
mvnw.cmd clean compile      # Windows
```

### 3. Start Services (in order)

**Important:** Start services in this order due to dependencies:

```bash
# Terminal 1: Service Registry (must start first)
cd service-registry
../mvnw spring-boot:run

# Terminal 2: User Service
cd user-service
../mvnw spring-boot:run

# Terminal 3: Product Service
cd product-service
../mvnw spring-boot:run

# Terminal 4: Inventory Service
cd inventory-service
../mvnw spring-boot:run

# Terminal 5: Payment Service
cd payment-service
../mvnw spring-boot:run

# Terminal 6: Order Service
cd order-service
../mvnw spring-boot:run

# Terminal 7: Notification Service
cd notification-service
../mvnw spring-boot:run

# Terminal 8: API Gateway (start last)
cd api-gateway
../mvnw spring-boot:run
```

### 4. Access the Services

| URL | Description |
|-----|-------------|
| http://localhost:8761 | Eureka Dashboard |
| http://localhost:8081/swagger-ui.html | User Service API Docs |
| http://localhost:8082/swagger-ui.html | Product Service API Docs |
| http://localhost:8083/swagger-ui.html | Order Service API Docs |
| http://localhost:8084/swagger-ui.html | Payment Service API Docs |
| http://localhost:8085/swagger-ui.html | Inventory Service API Docs |
| http://localhost:8081/h2-console | User DB Console |

## 🔄 Order Processing Flow (SAGA)

```
1. Client → POST /api/orders → Order Service
2. Order Service → [ORDER_CREATED] → Kafka
3. Kafka → Inventory Service (reserve stock)
4. Inventory Service → [INVENTORY_RESERVED] → Kafka
5. Kafka → Payment Service (process payment)
6. Payment Service → [PAYMENT_COMPLETED] → Kafka
7. Kafka → Order Service (confirm order)
8. Order Service → [ORDER_CONFIRMED] → Kafka
9. Kafka → Notification Service (send notification)
```

**On failure (e.g., payment declined):**
- Payment Service publishes PAYMENT_FAILED
- Order Service marks order as PAYMENT_FAILED
- Order Service publishes ORDER_CANCELLED
- Inventory Service releases reserved stock (compensation)

## 🎨 Design Patterns

| Pattern | Where | Purpose |
|---------|-------|---------|
| **SAGA (Orchestration)** | Order Service | Distributed transaction management |
| **Strategy** | Payment, Notification | Multiple payment methods, notification channels |
| **Factory** | Payment | Runtime strategy selection |
| **Observer** | Order (via Kafka) | Event-driven state changes |
| **Adapter** | Payment | Payment gateway abstraction |
| **API Gateway** | Gateway | Single entry point |
| **Service Discovery** | Eureka | Dynamic routing |

## 🔐 Security

- **JWT Authentication** — Stateless, token-based
- **BCrypt Password Hashing** — Industry standard
- **RBAC** — ADMIN and CUSTOMER roles
- **Gateway-level Auth Filter** — Validates token presence
- **CSRF Disabled** — Appropriate for stateless APIs

## 🗄️ Database

- **Development**: H2 in-memory (PostgreSQL compatibility mode)
- **Production-ready**: Swap to PostgreSQL by changing `application.yml`

Each service has its own database (Database per Service pattern).

## 📁 Project Structure

```
PaymentSystem/
├── pom.xml                      # Parent POM (dependency management)
├── common-lib/                  # Shared DTOs, events, exceptions
├── service-registry/            # Eureka Server
├── api-gateway/                 # Spring Cloud Gateway
├── user-service/                # User management + JWT auth
├── product-service/             # Product catalog + caching
├── order-service/               # Order management + SAGA
├── payment-service/             # Payment processing + strategies
├── inventory-service/           # Stock management + locking
├── notification-service/        # Event-driven notifications
├── HLD.md                       # High-Level Design document
├── ASSUMPTIONS.md               # Assumptions and decisions
├── CHANGELOG.md                 # Change log
└── README.md                    # This file
```

## 🔧 Git Workflow

```bash
# Initialize repository
git init
git add .
git commit -m "Initial commit: Payment System microservices"

# Create develop branch
git checkout -b develop

# For each feature:
git checkout -b feature/your-feature
# ... make changes ...
git add .
git commit -m "feat: description of changes"
git checkout develop
git merge feature/your-feature

# When ready for release:
git checkout main
git merge develop
```

## 📝 API Quick Reference

### Register & Login
```bash
# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@test.com","password":"password123"}'

# Login (returns JWT token)
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@test.com","password":"password123"}'
```

### Create Product & Order
```bash
# Create Category (use token from login)
curl -X POST http://localhost:8082/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Electronic devices"}'

# Create Product
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99,"categoryId":1}'

# Add Inventory
curl -X POST http://localhost:8085/api/inventory \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"totalQuantity":100}'

# Create Order
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"items":[{"productId":1,"productName":"Laptop","quantity":1,"unitPrice":999.99}]}'
```
