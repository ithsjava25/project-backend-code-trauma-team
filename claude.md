# AI-Assisted Architecture Guardrails (MVC Architecture)

This repo follows a clean **Model-View-Controller (MVC)** design pattern. Keep the architecture predictable by following these conventions every time you add or change code.

---

## Goals (what we optimize for)

1. **Security first:** GitHub OAuth2 authentication and role-based access control.
2. **Clear layering:** Strict separation between DTOs (Web), Services (Logic), and Entities (DB).
3. **Simplicity:** No unnecessary abstraction layers (no Domain models or Ports/Adapters).
4. **Transactional Integrity:** Business operations are wrapped in SQL transactions using `@Transactional`.

---

## Project Stack

- **Java 25**
- **Spring Boot 4.0.4** (Web + Data JPA + Security OAuth2)
- **PostgreSQL** (Production) / **H2** (Tests)
- **Thymeleaf** templates for the UI
- **GitHub OAuth2** for Authentication

---

## Core Architectural Components

For every feature (e.g., "Case"), we maintain this consistent set of components:

### 1. `*Controller` (Presentation Layer)
- **Role:** Handles incoming HTTP requests (REST API or Thymeleaf Web).
- **Location:** `...presentation.rest` or `...presentation.web`
- **Responsibility:** Thin logic only. Translates between HTTP and DTOs.
- **Injected with:** `*Service`.

### 2. `*DTO` (Data Transfer Object)
- **Role:** Data containers for web/API interaction.
- **Location:** `...presentation.dto`
- **Responsibility:** Prevents exposing internal database structures to the client.

### 3. `*Service` (Application/Logic Layer)
- **Role:** The core business logic handler.
- **Location:** `...application.service`
- **Responsibility:** Handles `@Transactional` boundaries, business rules, and security checks.
- **Injected with:** `*Repository` and `*Mapper`.

### 4. `*Mapper` (Application/Logic Layer)
- **Role:** Utility for object conversion.
- **Location:** `...application.service`
- **Responsibility:** Mapping `DTO <-> Entity`.

### 5. `*Entity` (Persistence Layer)
- **Role:** JPA-mapped object representing the database schema.
- **Location:** `...infrastructure.persistence`

### 6. `*Repository` (Infrastructure Layer)
- **Role:** Database access via Spring Data JPA.
- **Location:** `...infrastructure.persistence`

---

## Package Layout

```text
src/main/java/
  org/example/projektarendehantering/
    common/         (Cross-cutting utilities: Actor, Exceptions, Roles)
    application/
      service/      (Services, Mappers)
    presentation/
      rest/         (REST Controllers)
      web/          (UI Controllers)
      dto/          (DTOs)
    infrastructure/
      persistence/  (Entities, Repositories)
      config/       (Spring Security / OAuth2 Config)
      security/     (Authentication/Authorization logic)
```

---

## Naming Conventions

1. **Controllers:** Suffix with `Controller` (e.g., `CaseController`).
2. **Services:** Suffix with `Service` (e.g., `CaseService`).
3. **Mappers:** Suffix with `Mapper` (e.g., `CaseMapper`).
4. **DTOs:** Suffix with `DTO` (e.g., `CaseDTO`).
5. **Entities:** Suffix with `Entity` (e.g., `CaseEntity`).
6. **Repositories:** Suffix with `Repository` (e.g., `CaseRepository`).

---

## Security & Authorization

- **Authentication:** Handled via GitHub OAuth2.
- **Authorization:** Enforced in the `Service` layer or via `@PreAuthorize`.
- **Identity:** The current user is represented by the `Actor` class, derived from the OAuth2 session.

---

## AI Tool Usage Rules

1. **Always use DTOs** for public API communication.
2. **Never expose Entities** directly to the web layer.
3. **Use Mappers** to handle the translation between layers.
4. **Ensure @Transactional** is used on Service methods that modify data.
5. **Verify changes** with `mvnw compile` before finishing.
6. **No "Domain" classes:** Logic belongs in Services or Entities.
7. **No "Ports/Adapters":** Use direct Repository/Service injection.
