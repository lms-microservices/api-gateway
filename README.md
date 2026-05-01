# API Gateway

A robust and scalable API Gateway service for the LMS Microservices ecosystem, providing centralized request routing, authentication, rate limiting, and service orchestration.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Architecture](#architecture)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

The API Gateway serves as the single entry point for all client requests in the LMS microservices architecture. It handles cross-cutting concerns such as authentication, request routing, rate limiting, and load balancing, allowing individual microservices to focus on their core business logic.

## ✨ Features

- **Request Routing**: Intelligent routing of requests to appropriate microservices
- **Authentication & Authorization**: JWT-based authentication and role-based access control
- **Rate Limiting**: Configurable rate limiting to prevent service abuse
- **Request/Response Transformation**: Header manipulation and payload transformation
- **Service Discovery**: Dynamic service discovery and load balancing
- **Monitoring & Logging**: Comprehensive request/response logging and metrics collection
- **Circuit Breaker**: Automatic failure detection and circuit breaking
- **API Versioning**: Support for multiple API versions
- **CORS Support**: Configurable Cross-Origin Resource Sharing

## 🛠 Tech Stack

- **Language**: Java
- **Framework**: Spring Cloud Gateway / Spring Boot
- **Service Discovery**: Eureka / Consul (configurable)
- **Authentication**: JWT
- **Monitoring**: Spring Boot Actuator, Micrometer, Prometheus
- **Containerization**: Docker
- **Orchestration**: Kubernetes (optional)

## 📦 Prerequisites

- Java 11 or higher
- Maven 3.6+ or Gradle 6.0+
- Docker (for containerized deployment)
- Service Registry (Eureka/Consul)

## 🚀 Installation

### Clone the Repository

```bash
git clone https://github.com/lms-microservices/api-gateway.git
cd api-gateway
```

### Build the Project

Using Maven:
```bash
mvn clean install
```

Using Gradle:
```bash
gradle clean build
```

### Build Docker Image

```bash
docker build -t lms-microservices/api-gateway:latest .
```

## ⚙️ Configuration

### Environment Variables

Create a `.env` file or set the following environment variables:

```env
# Server Configuration
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/

# Service Registry
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka
SERVICE_REGISTRY_TYPE=eureka  # or 'consul'

# Authentication
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=3600000

# Rate Limiting
RATE_LIMIT_ENABLED=true
RATE_LIMIT_REQUESTS=1000
RATE_LIMIT_WINDOW=60

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_LMS_MICROSERVICES=DEBUG
```

### Application Configuration

Edit `application.yml` or `application.properties`:

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: users-service
          uri: lb://users-service
          predicates:
            - Path=/api/v1/users/**
        - id: courses-service
          uri: lb://courses-service
          predicates:
            - Path=/api/v1/courses/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: GET, POST, PUT, DELETE, OPTIONS
            allowedHeaders: "*"

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

## 📖 Usage

### Starting the Service

Local development:
```bash
mvn spring-boot:run
```

Using Docker:
```bash
docker run -p 8080:8080 --env-file .env lms-microservices/api-gateway:latest
```

Kubernetes deployment:
```bash
kubectl apply -f k8s/
```

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Available Endpoints

- `GET /actuator/health` - Service health status
- `GET /actuator/metrics` - Application metrics
- `GET /swagger-ui.html` - API documentation (if Swagger enabled)

## 🏛️ Architecture

```
┌─────────────────┐
│   Clients       │
└────────┬────────┘
         │ HTTP/REST
         ▼
┌─────────────────────────┐
│   API Gateway           │
│ ┌─────────────────────┐ │
│ │ Authentication      │ │
│ │ Rate Limiting       │ │
│ │ Request Routing     │ │
│ │ Load Balancing      │ │
│ └─────────────────────┘ │
└────────┬────────────────┘
         │
    ┌────┴────┬──────────┬──────────┐
    ▼         ▼          ▼          ▼
┌────────┐ ┌──────┐  ┌────────┐ ┌──────┐
│ Users  │ │Course│  │Payments│ │Notify│
│Service │ │Service  │Service │ │Service
└────────┘ └──────┘  └────────┘ └──────┘
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Google Java Style Guide
- Write unit tests for new features (minimum 80% coverage)
- Ensure all tests pass: `mvn clean test`
- Update documentation as needed

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📧 Support

For support and questions, please open an issue in the repository or contact the development team at [support@lms-microservices.dev](mailto:support@lms-microservices.dev).

---

**Last Updated**: April 29, 2026  
**Maintained By**: LMS Microservices Team
