# Trauma Team - Case Management System

A robust, full-stack Spring Boot application designed for medical case management, featuring a clean MVC architecture, hybrid authentication, and cloud-native document storage.

## Project Summary

The "Trauma Team Case Management" project provides a secure platform for managing patient cases, employee assignments, and medical documentation. It follows strict architectural guardrails to ensure scalability, security, and maintainability.

### Key Features

- **Case Management:** Full lifecycle management of medical cases (Create, Read, Update, Delete, Close).
- **Hybrid Authentication:**
  - **Patients:** Register and login using traditional email/password credentials.
  - **Employees/Managers:** Secure login via **GitHub OAuth2** integration.
- **Role-Based Access Control (RBAC):** Fine-grained permissions for Patients, Employees, and Managers.
- **Document Management:** Attach documents to cases with secure storage in **S3-compatible** systems (MinIO/Cloudflare R2).
- **Resilience:** Built-in retry logic for file operations and background processing for failed S3 deletions.
- **Auditing:** Automatic audit logging of key system events and user actions.
- **Caching:** Performance optimization using **Caffeine** for frequent data lookups.
- **Responsive UI:** Modern, clean web interface built with **Thymeleaf** and Vanilla CSS.

## Technologies Used

### Backend
- **Java 25**
- **Spring Boot 4.0.4**
- **Spring Data JPA** (Persistence)
- **Spring Security** (Authentication & Authorization)
- **Spring Cloud AWS** (S3 Integration)
- **Spring Retry** (Resilience)

### Database & Infrastructure
- **PostgreSQL** (Production/Development Database)
- **H2** (In-memory testing)
- **MinIO** (Local S3-compatible storage)
- **Docker & Docker Compose** (Containerized infrastructure)

### Frontend
- **Thymeleaf** (Server-side templating)
- **Vanilla CSS** (Custom styling)
- **Apache Tika** (MIME type detection)

### Developer Tools
- **Lombok** (Boilerplate reduction)
- **Spotless** (Code formatting)
- **Maven** (Build automation)
- **GitHub Actions** (CI/CD)

---

## Getting Started

### Prerequisites

- Java 25
- Docker Desktop (or Docker Engine with Compose)
- Maven Wrapper (`mvnw`) is included in the project

### 1. Start Infrastructure Services

The project uses Docker Compose to run PostgreSQL and MinIO.

```powershell
# Quick start (recommended on Windows)
.\start-local.ps1
```

Or manually:

```bash
docker compose up -d
```

This starts:
- **PostgreSQL:** `localhost:5432`
- **MinIO API:** `localhost:9000`
- **MinIO Console:** `localhost:9001`

### 2. Configure GitHub OAuth2

To use GitHub login, you need to set your environment variables:
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`

### 3. Run the Application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The application will be available at [http://localhost:8080](http://localhost:8080).

## Access and Roles

### Admin/Manager Account
A default manager account is seeded when running with the `local` profile:
- **Email:** `admin@traumateam.com`
- **Password:** `password`
- **Role:** `MANAGER`

### Patient Access
Patients can register at `/register` and manage their own cases.

### Employee Access
Employees should use the "Sign in with GitHub" option on the login page.

---

## Project Structure

The project follows a strict **MVC** architecture:
- `presentation`: REST and Web controllers, DTOs.
- `application`: Business logic (Services), Mappers.
- `infrastructure`: Persistence (Entities, Repositories), Security configuration, Cloud config.
- `common`: Cross-cutting concerns (Exceptions, Shared Models).

## Troubleshooting

- **Port Conflicts:** If port `5432` is in use, stop any local PostgreSQL services or modify the mapping in `docker-compose.yml` and `application.properties`.
- **Docker Issues:** Ensure Docker Desktop is running and the Linux engine is active. Use `docker version` to verify.
