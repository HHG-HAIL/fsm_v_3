# Task Management Service (task-svc)

## Overview
The Task Management Service is a Spring Boot microservice that manages field service tasks within the FSM (Field Service Management) system. This service provides domain models and repository interfaces for managing service tasks with various statuses and priorities.

## Technology Stack
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (for development/testing)
- **PostgreSQL** (for production)
- **Lombok** (reduces boilerplate code)
- **Swagger/OpenAPI** (API documentation)

## Domain Model

### ServiceTask Entity
Represents a field service task with the following fields:
- `id` (Long): Unique identifier
- `title` (String): Task title (minimum 3 characters, required)
- `description` (String): Detailed task description
- `clientAddress` (String): Customer location address (required)
- `priority` (Enum): Task priority - HIGH, MEDIUM, or LOW (required)
- `estimatedDuration` (Integer): Estimated duration in minutes (must be positive if provided)
- `status` (Enum): Task status - UNASSIGNED, ASSIGNED, IN_PROGRESS, or COMPLETED (defaults to UNASSIGNED)
- `createdBy` (String): User who created the task
- `createdAt` (LocalDateTime): Task creation timestamp

### Domain Invariants
- Task must have a title with at least 3 characters
- Task must have a valid client address
- Task must have a priority (HIGH, MEDIUM, LOW)
- Task status must be one of the valid enum values
- Estimated duration must be positive if provided

### Lifecycle Methods
- `assign()`: Changes task status from UNASSIGNED to ASSIGNED
- `start()`: Changes task status from ASSIGNED to IN_PROGRESS
- `complete()`: Changes task status from IN_PROGRESS to COMPLETED
- `isCompleted()`: Checks if task is completed
- `isUnassigned()`: Checks if task is unassigned

## Repository

### TaskRepository
Spring Data JPA repository providing:
- Standard CRUD operations (via JpaRepository)
- Custom query methods:
  - `findByStatus(TaskStatus status)`
  - `findByPriority(Priority priority)`
  - `findByCreatedBy(String createdBy)`
- `getHardcodedTasks()`: Returns 6 sample tasks for initial development

### Hardcoded Sample Tasks
The repository includes 6 hardcoded tasks with various:
- Priorities: HIGH, MEDIUM, LOW
- Statuses: UNASSIGNED, ASSIGNED, IN_PROGRESS, COMPLETED
- Different addresses, descriptions, and creators

## Configuration

### Application Properties
- **Server Port**: 8081
- **Database**: H2 in-memory database (development)
- **JPA**: Hibernate with create-drop DDL auto
- **Swagger UI**: Available at `/swagger-ui.html`
- **API Docs**: Available at `/api-docs`

## Testing
The service includes comprehensive unit and integration tests:
- **70 total tests**
- **90% code coverage** (exceeds 85% requirement)
- Domain model validation tests
- Repository integration tests
- Lifecycle transition tests

## Building and Running

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Run Application
```bash
mvn spring-boot:run
```

The application will start on port 8081.

## Code Coverage
To generate and view code coverage report:
```bash
mvn test
# Report available at: target/site/jacoco/index.html
```

## Future Enhancements
This initial implementation provides the domain model with hardcoded data. Future tasks will add:
- REST API controllers
- Service layer with business logic
- Database migrations with Flyway
- Integration with other microservices
- Advanced querying and filtering
