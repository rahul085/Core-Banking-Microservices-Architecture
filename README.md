🏦 Core Banking Microservices Architecture

An enterprise-grade, event-driven Core Banking System built with Spring Boot 3.x and Java 21.

This project showcases real-world distributed system design using:

Saga Orchestration (Temporal)
Transactional Outbox Pattern (Kafka)
Resilient Microservices (Resilience4j)
🚀 Overview

This system simulates a modern banking backend where multiple services collaborate to perform secure and reliable financial transactions.

✔ Fully decoupled microservices
✔ Event-driven communication
✔ Strong consistency with Saga orchestration
✔ Production-grade security and resilience

🏗️ High-Level Architecture

The system is divided into independent microservices communicating via:

Synchronous Communication → REST APIs (via API Gateway)
Asynchronous Communication → Apache Kafka events

👉 Ensures:
Loose coupling
High scalability
Fault tolerance

✔ Distributed consistency is maintained using Temporal workflows
✔ Ensures all-or-nothing transactions (Saga pattern)

🧩 Microservices Overview
🔹 API Gateway (:8080)
Single entry point
JWT validation & authentication
Request routing
Injects headers (X-User-Id)

🔹 Eureka Discovery Server (:7001)
Service registry
Enables dynamic service discovery
Load balancing support

🔹 Auth Service (:7005)
User registration & login
JWT token generation (Access + Refresh)
Central identity provider

🔹 Account Service (:7002)
Manages bank accounts
Handles debit/credit operations
Maintains account balances

🔹 Transaction Service (:8083)
Core orchestration engine
Validates security (BOLA/IDOR protection)
Starts Temporal workflows
Implements Transactional Outbox pattern

🔹 Notification Service (:7004)
Kafka consumer
Sends email notifications
Fetches user data from Auth Service


⚙️ Key Architectural Patterns

🔁 Saga Pattern (Orchestration)
Managed using Temporal
Handles multi-step transactions
Supports rollback (compensation logic)

📦 Transactional Outbox Pattern
Prevents dual-write problems
Writes DB + event in single transaction
Reliable Kafka publishing via relay

📡 Event-Driven Architecture
Powered by Apache Kafka
Decouples services
Enables async communication

🔐 Security (BOLA / IDOR Protection)
Verifies ownership of resources
Prevents unauthorized account access
JWT-based authentication


⚡ Resilience & Fault Tolerance
Resilience4j:
Circuit Breaker
Rate Limiting
Prevents cascading failures


💻 Tech Stack
Category	Technologies
Core	Java 21, Spring Boot 3.x
Microservices	Spring Cloud (Gateway, Eureka)
Database	PostgreSQL / Oracle DB
ORM	Spring Data JPA, Hibernate
Messaging	Apache Kafka
Orchestration	Temporal
Security	Spring Security, JWT
Resilience	Resilience4j
Tools	Maven, Lombok, Postman

🛠️ Local Setup & Execution
📌 Prerequisites

Before running services, ensure:

✅ Database (Postgres / Oracle) is running
✅ Kafka & Zookeeper running on 9092
✅ Temporal Server running
UI → http://localhost:8080
gRPC → localhost:7233

▶️ Boot Order (IMPORTANT)

Start services in this order:
discovery-server
auth-service
account-service
transaction-service
notification-service
api-gateway

🚦 API Usage Flow
1️⃣ Authenticate
POST http://localhost:8080/api/v1/auth/login

Request Body:
{
  "email": "user@example.com",
  "password": "password"
}

Response:
JWT Access Token

2️⃣ Initiate Transfer
POST http://localhost:8080/api/v1/transactions/transfer

Headers:

Authorization: Bearer <JWT_TOKEN>
Idempotency-Key: unique-uuid

Body:

{
  "fromAccountId": 100,
  "toAccountId": 200,
  "amount": 500.00
}

3️⃣ Internal Flow (Behind the Scenes)
API Gateway validates JWT
Request routed to Transaction Service
Rate limiting + ownership validation
Temporal starts Saga workflow
Account Service performs debit/credit
Transaction saved + Outbox event created
Kafka publishes event
Notification Service sends email

🧠 Key Highlights (Interview Focus)
✅ Distributed transaction handling without 2PC
✅ Exactly-once event publishing (Outbox pattern)
✅ Secure microservices with JWT
✅ Scalable & fault-tolerant design
✅ Real-world banking use case implementation

🗺️ Roadmap / Future Enhancements
 Loan Service
Loan workflows using Temporal
EMI calculations (flat & reducing)
 Dockerization
Full system via docker-compose
 Audit Service
Read-optimized reporting service
Transaction history & statements

📌 Conclusion
This project demonstrates production-grade microservices architecture with: 
Strong consistency guarantees
High scalability
Robust security
Fault tolerance
It reflects real-world backend design used in modern fintech and banking systems.
