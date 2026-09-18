# Leave Management System
### COMP60047 Enterprise Application Development

A Leave Management System built with **Domain-Driven Design (DDD)**, **CQRS**, and **Spring Boot 3.2.5**. The system supports three roles: EMPLOYEE, MANAGER, and ADMIN, covering the full leave lifecycle from application through to approval, rejection, amendment, and cancellation, with allowance tracking throughout.

---

## Tech Stack

| | |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security · JWT (HMAC-SHA384) · BCrypt |
| Persistence | Spring Data JPA · H2 (test/dev) · PostgreSQL (prod) |
| Build | Maven |
| Testing | JUnit 5 · Mockito · MockMvc (64 tests) |
| Frontend | Vanilla JS · HTML served as static files |

---

## Architecture

The system is structured around four strict DDD layers. Dependencies only flow inward; the domain layer has zero Spring imports.

```
HTTP Request → Controller → Facade → CommandHandler / QueryHandler → Domain → Repository
```

| Layer | Responsibility | Key Packages |
|-------|---------------|-------------|
| Domain | Entities, business invariants, repository interfaces | `domain/model/`, `domain/repository/`, `domain/event/` |
| Application | Commands, queries, facade, DTOs, mappers, exceptions, event listeners | `application/command/`, `application/query/`, `application/facade/` |
| Infrastructure | JPA persistence, Spring Security, JWT, rate limiting, event store, data seeding | `infrastructure/security/`, `infrastructure/ratelimit/`, `infrastructure/eventstore/` |
| Interface | REST controllers, web dashboard controllers, global exception handler | `interfaces/rest/`, `interfaces/web/` |

### CQRS

Write and read concerns are separated into distinct handlers coordinated by a single facade:

- `LeaveCommandHandler`: create, approve, reject, amend, cancel, amendAllowance
- `LeaveQueryHandler`: getLeaveById, getLeavesByEmployee, getPendingLeaves, getAllowance, getTeamStats
- `AuthCommandHandler`: register, login
- `EmployeeQueryHandler`: getAllEmployees, getEmployeeById, getEmployeeByEmail
- `LeaveContextFacade`: single entry point; controllers never depend on individual handlers directly

### Domain Aggregates

`Leave` and `LeaveAllowance` are rich aggregates; all business rules are enforced inside the entity:

- `Leave.approve()`: throws `IllegalStateException` if status is not PENDING
- `Leave.reject()`: requires a non-null `RejectionReason`; throws if not PENDING
- `Leave.amend()`: validates date ordering; throws if not PENDING
- `Leave.cancel()`: validates ownership (employee ID must match); throws if already CANCELLED
- `LeaveAllowance.deduct()`: throws `IllegalStateException` if insufficient balance
- `LeaveAllowance.restore()`: uses `Math.max(0, ...)` to prevent negative used days
- `LeaveAllowance.amendTotalDays()`: validates positive total

`RejectionReason` is modelled as a JPA `@Embeddable` value object, not a plain string.

---

## Security

- **JWT**: stateless authentication, HMAC-SHA384, 24-hour expiry
- **HttpOnly cookie**: JWT stored in `HttpOnly`, `SameSite=Strict` cookie for the browser frontend, preventing XSS-based token theft
- **RBAC**: enforced at three independent layers:
  1. `SecurityFilterChain` URL matchers (e.g. `/api/admin/**` requires `hasRole("ADMIN")`)
  2. `@PreAuthorize` method annotations on controllers
  3. Domain ownership checks inside aggregate methods (e.g. `Leave.cancel()`)
- **Rate limiting**: sliding window, 5 requests/minute/IP on `/api/auth/login`, implemented with `ConcurrentHashMap<String, Deque<Long>>`
- **Header obfuscation**: `Server` and `X-Powered-By` headers cleared on every response
- **Log injection prevention**: email and IP fields sanitised before logging in `AuthCommandHandler`

---

## Event-Driven Audit Trail

When leave is approved or rejected, a domain event is published via Spring's `ApplicationEventPublisher`:

- `@TransactionalEventListener(phase = AFTER_COMMIT)`: event only fires if the DB transaction committed, preventing phantom events on rollback
- `@Async`: listener runs on a separate thread pool, does not block the HTTP response
- `EventStoreService` persists each event as an immutable JSON record in the `event_store` table using `Propagation.REQUIRES_NEW`

---

## Running the Application

### Prerequisites

- Java 17
- Maven 3.8+

### Run (H2 in-memory, no external DB required for tests)

```bash
mvn spring-boot:run
```

The app starts at `http://localhost:8080`. Three users are seeded automatically on startup:

```
Manager:  manager@example.com / password123
Employee: employee@example.com / password123
Admin:    admin@example.com / password123
```

### Run Tests

```bash
mvn test
```

All 64 tests run against an in-memory H2 database with no external dependencies required.

---

## Frontend

Three role-specific dashboards served as static HTML:

| Role | URL | Features |
|------|-----|---------|
| Employee | `/employee/dashboard` | Apply, amend, cancel leave · view allowance |
| Manager | `/manager/dashboard` | View pending requests · approve · reject · team stats |
| Admin | `/admin/dashboard` | View and amend employee allowances |

Login at `http://localhost:8080/login.html`. The web controller reads the JWT cookie, resolves the role, and redirects to the correct dashboard. The client never self-selects its own view.

H2 console (dev): `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa` · Password: *(empty)*

---

## API Reference

### Auth — public

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register a new employee (returns JWT) |
| `POST` | `/api/auth/login` | Login (returns JWT) |

### Leaves — authenticated (EMPLOYEE)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/leaves` | Apply for leave |
| `GET` | `/api/leaves/my` | Get own leave requests |
| `GET` | `/api/leaves/{id}` | Get leave by ID |
| `PUT` | `/api/leaves/{id}/amend` | Amend a PENDING leave |
| `PUT` | `/api/leaves/{id}/cancel` | Cancel own leave |
| `GET` | `/api/leaves/allowance` | Get own leave allowance |
| `GET` | `/api/leaves/allowance/history` | Get allowance history |

### Leaves — MANAGER only

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/leaves/pending` | Get pending requests for own team |
| `PUT` | `/api/leaves/{id}/approve` | Approve leave (deducts from allowance) |
| `PUT` | `/api/leaves/{id}/reject` | Reject leave with optional reason body |
| `GET` | `/api/leaves/team-stats` | Get team leave statistics |

### Admin — ADMIN only

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/allowances/{employeeId}` | Get any employee's allowance |
| `PUT` | `/api/admin/allowances/{employeeId}` | Amend any employee's total days |

### Employees — authenticated

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/employees` | Get all employees |
| `GET` | `/api/employees/{id}` | Get employee by ID |

---

## Testing

64 tests across four classes:

| Class | Type | Tests |
|-------|------|-------|
| `LeaveTest` | Domain unit: no Spring, no Mockito, no DB | 13 |
| `LeaveCommandHandlerTest` | Application layer: Mockito mocks | 5 |
| `LeaveQueryHandlerTest` | Application layer: Mockito mocks | 4 |
| `LeaveSystemEndToEndTest` | Full HTTP stack: `@SpringBootTest` + `MockMvc` | 42 |

Integration tests cover: authentication, RBAC enforcement, leave creation validation, approval/rejection, cancellation, amendment, allowance deduction and restoration, team stats, employee queries, and admin operations.

---

## Project Structure

```
src/
├── main/java/com/example/leave/
│   ├── domain/
│   │   ├── model/          Leave.java, LeaveAllowance.java, Employee.java,
│   │   │                   RejectionReason.java, LeaveStatus.java, LeaveType.java, Role.java
│   │   ├── repository/     LeaveRepository.java, LeaveAllowanceRepository.java, EmployeeRepository.java
│   │   └── event/          LeaveApprovedEvent.java, LeaveRejectedEvent.java
│   ├── application/
│   │   ├── command/        LeaveCommandHandler.java, AuthCommandHandler.java
│   │   ├── query/          LeaveQueryHandler.java, EmployeeQueryHandler.java
│   │   ├── facade/         LeaveContextFacade.java
│   │   ├── dto/            CreateLeaveCommand, AmendLeaveCommand, LeaveDto, LeaveAllowanceDto, ...
│   │   ├── event/          LeaveEventListener.java
│   │   ├── mapper/         LeaveMapper.java, LeaveAllowanceMapper.java, EmployeeMapper.java
│   │   └── exception/      LeaveRequestNotFoundException, EmployeeNotFoundException,
│   │                       LeaveAllowanceNotFoundException, DuplicateEmailException
│   ├── infrastructure/
│   │   ├── security/       SecurityConfig.java, JwtService.java, JwtAuthFilter.java
│   │   ├── ratelimit/      RateLimitFilter.java
│   │   ├── eventstore/     EventStore.java, EventStoreService.java, EventStoreRepository.java
│   │   └── DataSeeder.java
│   └── interfaces/
│       ├── rest/           LeaveController.java, AuthController.java, AdminController.java,
│       │                   EmployeeController.java, GlobalExceptionHandler.java
│       └── web/            AuthWebController.java, EmployeeDashboardController.java,
│                           ManagerDashboardController.java, AdminDashboardController.java
├── main/resources/
│   ├── static/             login.html, employee/dashboard.html, manager/dashboard.html,
│   │                       admin/dashboard.html, css/, 403.html
│   └── application.properties
└── test/
    ├── domain/             LeaveTest.java
    ├── application/        LeaveCommandHandlerTest.java, LeaveQueryHandlerTest.java
    ├── integration/        LeaveSystemEndToEndTest.java
    └── resources/          application.properties (H2 in-memory)
```

---

## Configuration

Production datasource and JWT secret are configured via environment variables:

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/leavedb
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
jwt.secret=${JWT_SECRET:your-secret-key}
jwt.expiration=86400000
server.server-header=
```

Tests use a separate `src/test/resources/application.properties` with H2 and a fixed test secret. No environment variables required.

---

## Postman Collection

`DDD_Leave_System.postman_collection.json` at the project root contains pre-built requests for all endpoints. Import into Postman and use the login request to obtain a JWT, then set it as a Bearer token on subsequent requests.
