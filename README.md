# 🛒 Spring Online Store - Microservices Architecture

A microservices-based online store built with **Spring Boot**, utilizing **Spring Cloud**, **Docker**, and **Service Discovery (Eureka)**. The system is modular and scalable, supporting key e-commerce functionality such as product management, inventory control, order processing, and user notifications.

---

## 📦 Project Structure

This repository is composed of multiple microservices and core components:

| Module              | Description |
|---------------------|-------------|
| `api-gateway`       | Handles all incoming requests and routes them to appropriate microservices. |
| `discovery-server`  | Eureka-based service registry for managing and discovering services dynamically. |
| `product-service`   | Manages product details including name, description, price, and metadata. |
| `inventory-service` | Tracks stock availability and inventory updates. |
| `order-service`     | Handles customer orders and coordinates with inventory and product services. |
| `notification-service` | Sends email or message-based notifications for order updates. |

---

## 🛠️ Tech Stack

- **Spring Boot**
- **Spring Cloud (Eureka, Gateway)**
- **Spring Cloud (Eureka, Gateway)**
- **Security (OAuth2 with Keycloak)**
- **Resilience4J Circuit Breaker**
- **Distributed Tracing (Zipkin)**
- **Event-Driven Architecture using Kafka**
- **Maven**
- **Java 17**
- **RESTful APIs**

---

## 📐 Architecture Diagram

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven
- Docker & Docker Compose

### Clone the Repository

```bash
git clone https://github.com/hamie-kalhoro/spring-online-store-microservices.git
cd spring-online-store-microservices
