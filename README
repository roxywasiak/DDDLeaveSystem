# DDD Leave System

A Leave Management System built with Domain-Driven Design, CQRS, and Spring Boot.

## Architecture

```
Controller → ContextFacade → CommandHandler/QueryHandler → Repository → Mapper → DTO → JSON
```

### Layers

| Layer | Responsibility |
|-------|---------------|
| **Domain** | Entities, value objects, domain invariants, repository interfaces |
| **Application** | Commands, Queries, Facade, DTOs, Mappers, Custom Exceptions |
| **Infrastructure** | Security (JWT, Spring Security), persistence config |
| **Interfaces** | REST Controllers (thin), GlobalExceptionHandler |

### CQRS Separation

- **Commands** (writes): `AuthCommandHandler`, `LeaveCommandHandler`
- **Queries** (reads): `EmployeeQueryHandler`, `LeaveQueryHandler`
- **Facade**: `LeaveContextFacade` — single entry point coordinating all handlers

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Security + JWT
- Spring Data JPA + H2
- Lombok
- Maven
- JUnit 5 + MockMvc (end-to-end tests)

## Running

```bash
mvn spring-boot:run
```

## Testing

```bash
mvn test
```

## API Endpoints

### Auth (public)
- `POST /api/auth/register` — Register new employee
- `POST /api/auth/login` — Login and receive JWT

### Leaves (authenticated)
- `POST /api/leaves` — Apply for leave
- `GET /api/leaves/my` — Get my leave requests
- `GET /api/leaves/{id}` — Get leave by ID
- `PUT /api/leaves/{id}/cancel` — Cancel own leave

### Leaves (MANAGER only)
- `GET /api/leaves/pending` — Get all pending requests
- `PUT /api/leaves/{id}/approve` — Approve leave
- `PUT /api/leaves/{id}/reject` — Reject leave

### Employees (authenticated)
- `GET /api/employees` — Get all employees
- `GET /api/employees/{id}` — Get employee by ID
