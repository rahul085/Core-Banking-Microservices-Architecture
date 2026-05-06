
🏦 Core Banking Microservices Architecture
An enterprise-grade, event-driven Core Banking System built with Spring Boot 3.x and Java 21. This project demonstrates advanced distributed system patterns, including Saga orchestration using Temporal, the Transactional Outbox Pattern with Apache Kafka, and fail-fast resilience using Resilience4j.

🏗️ High-Level Architecture
This system is decoupled into independent microservices communicating via synchronous REST APIs (protected by API Gateway & Circuit Breakers) and asynchronous event streams (Kafka).

Cross-service data consistency is guaranteed using Temporal orchestrations, ensuring that financial debits and credits either succeed entirely or trigger automated compensating transactions.

🧩 Microservices Inventory
API Gateway (:8080): The single entry point. Handles JWT validation, routing, and HTTP header injection (e.g., X-User-Id) for downstream services.

Eureka Discovery Server (:7001): Service registry for dynamic internal routing and load balancing.

Auth Service (:7005): Identity provider. Manages user registration, authentication, JWT generation, and serves as the centralized user directory.

Account Service (:7002): The financial ledger. Manages account creation, balances, and executes atomic debit/credit operations.

Transaction Service (:8083): The orchestration brain. Intercepts transfer requests, validates BOLA/IDOR security rules, initiates Temporal workflows, and saves events to the Outbox table.

Notification Service (:7004): Event-driven consumer. Listens to Kafka topics, hydrates data by querying the Auth service, and dispatches asynchronous email alerts.

🚀 Key Architectural Patterns Implemented
Saga Pattern (Orchestration): Utilizes Temporal to manage complex, multi-step financial transfers across the Account Service, guaranteeing distributed ACID properties without two-phase commit locks.

Transactional Outbox Pattern: Prevents dual-write failures. The Transaction Service writes the transaction state and the OutboxEvent to the database in a single local transaction, which a Relay then safely pushes to Kafka.

Zero-Data-Loss Event Streaming: Uses Apache Kafka to asynchronously decouple the core money-movement logic from side effects (like email notifications).

BOLA/IDOR Security Prevention: Synchronous, fail-fast verification ensures the authenticated JWT token matches the ownership of the source bank account before initiating any debits.

Circuit Breakers & Rate Limiting: Powered by Resilience4j. Protects synchronous inter-service HTTP calls from cascading failures and shields endpoints from DDoS or excessive request loads.

💻 Tech Stack
Core: Java 21, Spring Boot 3.x, Spring Cloud (Gateway, Eureka)

Database: PostgreSQL / Oracle DB, Spring Data JPA, Hibernate

Orchestration: Temporal

Messaging: Apache Kafka, Spring Kafka

Security: Spring Security, JWT (JSON Web Tokens)

Resilience: Resilience4j (Circuit Breaker, Rate Limiter)

Tooling: Lombok, Maven, Postman

🛠️ Local Setup & Execution (Without Docker)
1. Prerequisites Infrastructure
Before starting the Spring Boot applications, ensure the following background infrastructure is running:

Database: A relational database (Postgres/Oracle) running locally.

Kafka & Zookeeper: Running locally on port 9092.

Temporal Server: Running locally (accessible at localhost:8080 for the UI / 7233 for gRPC).

2. Boot Sequence
Because these are interdependent microservices, start them in this exact order to allow proper Eureka registration:

discovery-server (Eureka)

auth-service

account-service

transaction-service

notification-service

api-gateway

🚦 API Usage Flow
1. Authenticate

HTTP
POST http://localhost:8080/api/v1/auth/login
Body: { "email": "user@example.com", "password": "password" }
Response: Receives JWT active token.
2. Initiate Transfer

HTTP
POST http://localhost:8080/api/v1/transactions/transfer
Headers:
  Authorization: Bearer <JWT_TOKEN>
  Idempotency-Key: unique-uuid-per-request
Body: 
{
  "fromAccountId": 100,
  "toAccountId": 200,
  "amount": 500.00
}
3. The Background Symphony

API Gateway validates the JWT and routes to Transaction Service.

Transaction Service applies Resilience4j Rate Limiting and checks Account Service (via Circuit Breaker) to verify account ownership.

Temporal kicks off the Saga, debiting 100 and crediting 200 via the Account Service.

Transaction Service writes SUCCESS to the DB and PENDING to the Outbox.

The Relay publishes to Kafka (outbox_events topic).

Notification Service consumes the event, fetches user details, and sends completion emails via SMTP.

🗺️ Roadmap / Future Enhancements
[ ] Loan Service: Implementing complex temporal workflows for loan origination, flat/reducing balance calculations, and automated EMI scheduling.

[ ] Dockerization: Wrapping all services and infrastructure into a unified docker-compose.yml for single-click deployment.

[ ] Audit Service: Read-optimized service for statement generation.
