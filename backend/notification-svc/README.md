# Notification Service

Notification microservice for managing alert delivery in the FSM system.

## Overview

The Notification Service is responsible for:
- Managing notification delivery to users
- Supporting multiple delivery channels (PUSH, EMAIL, SMS)
- Tracking read/unread status of notifications
- Tracking delivery status for reliability

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database (development)
- PostgreSQL (production)
- Maven
- Lombok
- Swagger/OpenAPI

## Domain Model

### Notification Aggregate
The core aggregate that represents an alert delivery with the following properties:
- **id**: Unique identifier
- **userId**: Recipient user ID (required)
- **type**: Notification type (PUSH, EMAIL, SMS) - determines delivery channel
- **title**: Notification title (required)
- **message**: Notification message content (required)
- **data**: Additional JSON data payload
- **read**: Read status flag
- **sentAt**: Timestamp when notification was sent
- **deliveredAt**: Timestamp when notification was delivered
- **createdAt**: Timestamp when notification was created

### Domain Invariants
- Notification must have a recipient user (userId)
- Notification type determines delivery channel
- Delivery status tracked for reliability
- Read status tracks if notification has been viewed by user

## Building and Running

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Test
```bash
mvn test
```

### Test Coverage
```bash
mvn jacoco:report
```
Coverage report will be available at `target/site/jacoco/index.html`

## API Documentation

Once the service is running, Swagger UI is available at:
```
http://localhost:8083/swagger-ui.html
```

API documentation (OpenAPI spec):
```
http://localhost:8083/api-docs
```

## Configuration

The service uses port `8083` by default. This can be changed in `application.properties`.

## Related Stories

- STORY-012: Technician Notification on Task Assignment
