# AI-Assisted Architecture Guardrails (Case-Service Architecture)

This repo will be developed by different AI tools across a group. Keep the architecture clean and predictable by following these conventions every time you add or change code.

---

## Goals (what we optimize for)

1. Security first: authorization and data-access controls must be correct.
2. Clear layering: separation of concerns between DTOs, Services, and Entities.
3. Auditability: every important activity produces an audit record.
4. Transactional Integrity: business operations are wrapped in SQL transactions.

---

## Project Stack

- Java 25
- Spring Boot (Web + Data JPA) + Maven
- H2 Database (or SQL compatible)
- JUnit tests
- Thymeleaf templates

---

## Core Architectural Components

For every feature (e.g., "Case"), we maintain a consistent set of components:

### 1. `*Controller` (Presentation Layer)
- **Role:** Handles incoming HTTP requests and interacts with the frontend.
- **Location:** `...presentation.rest`
- **Responsibility:** Translates between HTTP payloads and DTOs. Thin logic only.
- **Injected with:** `*Service`.

### 2. `*DTO` (Data Transfer Object)
- **Role:** Temporary objects for frontend interaction.
- **Location:** `...presentation.dto`
- **Responsibility:** Placeholder for data before it is converted to an entity or returned to the client.

### 3. `*Entity` (Infrastructure/Persistence Layer)
- **Role:** The data object written to the database.
- **Location:** `...infrastructure.persistence`
- **Responsibility:** JPA-mapped object representing the database schema.

### 4. `*Mapper` (Application Layer)
- **Role:** Utility for object conversion.
- **Location:** `...application.service`
- **Responsibility:** Mapping `DTO -> Entity` and `Entity -> DTO`.

### 5. `*Service` (Application Layer)
- **Role:** The core business logic handler.
- **Location:** `...application.service`
- **Responsibility:** Handles SQL transactions (using `@Transactional`). 
- **Injected with:** `*Repository` and `*Mapper`.

### 6. `*Repository` (Infrastructure Layer)
- **Role:** Database access.
- **Location:** `...infrastructure.persistence`
- **Responsibility:** Extends `JpaRepository` for SQL operations.

---

## Package layout

```text
src/main/java/
  org/example/projektarendehantering/
    common/         (Cross-cutting utilities)
    domain/         (Core business logic / legacy domain models)
    application/
      service/      (Services, Mappers)
      ports/        (Interface boundaries)
    presentation/
      rest/         (Controllers)
      dto/          (DTOs)
      web/          (UI Controllers)
    infrastructure/
      persistence/  (Entities, Repositories)
      config/       (Spring Config)
```

---

## Naming conventions

1. **Controllers:** Suffix with `Controller` (e.g., `CaseController`).
2. **Services:** Suffix with `Service` (e.g., `CaseService`).
3. **Mappers:** Suffix with `Mapper` (e.g., `CaseMapper`).
4. **DTOs:** Suffix with `DTO` (e.g., `CaseDTO`).
5. **Entities:** Suffix with `Entity` (e.g., `CaseEntity`).
6. **Repositories:** Suffix with `Repository` (e.g., `CaseRepository`).

---

## Security & Authorization

- Authorization must be enforced in the `Service` layer or via Spring Security.
- Controllers should not perform heavy logic; they delegate to Services which verify permissions.

---

## AI Tool Usage Rules

1. **Always use DTOs** for public API communication.
2. **Never expose Entities** directly to the web layer.
3. **Use Mappers** to handle the translation between layers.
4. **Ensure @Transactional** is used on Service methods that modify data.
5. **Verify changes** with `mvnw compile` before finishing.
