# Location Service

The Location Service is responsible for tracking technician positions in real-time for the Field Service Management (FSM) system.

## Bounded Context

This service is part of the **Location Services** bounded context and provides:
- Real-time technician location tracking
- Location history for technicians
- Geospatial data validation
- Support for map display features

## Domain Model

### TechnicianLocation Entity

The main aggregate root for this service with the following fields:
- `id` - Unique identifier
- `technicianId` - Reference to the technician
- `latitude` - GPS latitude (-90 to 90)
- `longitude` - GPS longitude (-180 to 180)
- `accuracy` - Location accuracy in meters (must be positive)
- `timestamp` - Time when the location was recorded (must not be in future)
- `batteryLevel` - Device battery level (0-100, optional)

### Domain Invariants

- Latitude must be between -90 and 90
- Longitude must be between -180 and 180
- Accuracy must be positive (in meters)
- Location timestamp must not be in the future

## API Endpoints

(To be implemented in future tasks)

## Configuration

### Application Properties

```properties
server.port=8082
spring.application.name=location-svc
```

### Database

- Development: H2 in-memory database
- Production: PostgreSQL

## Building

```bash
cd backend/location-svc
mvn clean install
```

## Testing

```bash
mvn test
```

## Running

```bash
mvn spring-boot:run
```

## Swagger UI

When running, access the API documentation at:
- http://localhost:8082/swagger-ui.html

## Related Issues

- Story: STORY-018 - Real-time Technician Location Tracking
- Task: TASK-030 - Create Location Aggregate Domain Model
