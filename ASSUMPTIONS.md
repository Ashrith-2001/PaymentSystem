# Assumptions & Decisions Log

This file tracks all assumptions made and key decisions taken during the development of the Payment System.

---

## Architecture Decisions

| # | Decision | Rationale | Date |
|---|----------|-----------|------|
| 1 | **Monorepo with Maven multi-module** | Easier to manage for a learning project; all services in one repo with shared parent POM | 2026-04-16 |
| 2 | **SAGA Orchestration over Choreography** | Better visibility into transaction state; easier to debug and manage complex order flows | 2026-04-16 |
| 3 | **Embedded Kafka over external Kafka** | No external installation required; code is identical to production Kafka | 2026-04-16 |
| 4 | **H2 Database (PostgreSQL mode) for dev** | Zero installation; SQL and JPA code is identical to PostgreSQL; swappable via config | 2026-04-16 |
| 5 | **Caffeine cache over Redis** | No external installation; same Spring `@Cacheable` annotations work with Redis; swappable | 2026-04-16 |
| 6 | **Maven Wrapper (mvnw) over global Maven** | No need to install Maven globally; wrapper ships with project; reproducible builds | 2026-04-16 |
| 7 | **JWT for authentication over sessions** | Stateless auth fits microservices architecture; no server-side session storage needed | 2026-04-16 |
| 8 | **Generic mock payment gateway** | No external API dependency; simulates real-world scenarios (success, failure, timeout) | 2026-04-16 |

---

## Functional Assumptions

| # | Assumption | Impact |
|---|-----------|--------|
| 1 | A user can have multiple addresses but one primary address | Address management in User Service |
| 2 | Products belong to exactly one category | Simplified category hierarchy (no multi-category) |
| 3 | Orders contain one or more order items, each referencing a product | Standard e-commerce order model |
| 4 | Payment is processed after inventory is reserved (SAGA sequence) | Prevents charging for out-of-stock items |
| 5 | Notifications are fire-and-forget (no delivery guarantee tracking) | Simplified notification; logged for audit |
| 6 | A single payment per order (no split payments) | Simplifies payment flow |
| 7 | Stock reservation expires if payment is not completed (timeout) | Prevents indefinite stock holds |
| 8 | Admin users are seeded via startup data; no self-registration for admin | Security: admin creation is controlled |

---

## Technical Assumptions

| # | Assumption | Impact |
|---|-----------|--------|
| 1 | Java 22 is installed on the development machine | Confirmed via `java -version` |
| 2 | Maven is NOT installed globally; using Maven Wrapper | `mvnw` / `mvnw.cmd` bundled with project |
| 3 | No Docker available; all services run as standalone JVM processes | Each service starts on its own port |
| 4 | No external message broker; using embedded Kafka for testing | Kafka config uses `spring-kafka-test` |
| 5 | No external cache server; using Caffeine in-memory cache | Cache is local to each JVM instance |
| 6 | Single developer environment (no CI/CD pipeline) | Manual build and test |
