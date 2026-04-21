# Projekt-arendehantering

Spring Boot application for case management with a Thymeleaf UI, PostgreSQL, and MinIO.

## Prerequisites

- Java 25
- Docker Desktop (or Docker Engine with Compose)
- Maven Wrapper (`mvnw`) is included in the project

## Start the application (local development)

### Quick start (recommended on Windows)

Run one command to perform all startup steps:

```powershell
.\start-local.ps1
```

Show full command output instead of fun/quiet mode:

```powershell
.\start-local.ps1 --serious
```

The script will:
- Verify Docker is available and the engine is running
- Start infrastructure with `docker compose up -d`
- Start Spring Boot with profile `local`

### Quick start (macOS/Linux with PowerShell)

Run the same startup script through `pwsh`:

```bash
pwsh ./start-local.ps1
```

Show full command output instead of fun/quiet mode:

```bash
pwsh ./start-local.ps1 --serious
```

### 0) Verify Docker is running (Windows)

Before running `docker compose up -d`, make sure Docker Desktop is started and the Linux engine is running.

1. Start Docker Desktop.
2. Wait until Docker Desktop shows **Engine running**.
3. Verify in PowerShell:

```powershell
docker version
```

If Docker is healthy, this command prints both `Client` and `Server` sections.

If you get an error like:

`open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified`

it means Docker Desktop is not running (or not fully started yet). Start/restart Docker Desktop, wait 10-30 seconds, then run `docker version` again.

### 1) Start infrastructure services

```bash
docker compose up -d
```

This starts:
- PostgreSQL on `localhost:5432`
- MinIO API on `localhost:9000`
- MinIO Console on `localhost:9001`

### 2) Start the Spring Boot application with the local profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows PowerShell:

```powershell
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

### 3) Open the app

- [http://localhost:8080](http://localhost:8080)

## Login and access

### Admin account (use normal login page)

There is no separate admin login page. Use the normal login form at [http://localhost:8080/login](http://localhost:8080/login).

When running with profile `local`, an admin-level account is seeded from `data-local.sql`:

- Email: `admin@traumateam.com`
- Password: `password`
- Role: `MANAGER`

Log in with:

- Username/Email: `admin@traumateam.com`
- Password: `password`

### Other login options

- Patients can register via `/register` and then log in with email/password at `/login`.
- Employees can use GitHub OAuth via the "Sign in with GitHub" button on `/login`.

## Stop services

```bash
docker compose down
```

## Quick troubleshooting

- `docker compose up -d` fails with pipe/engine error:
  - Start Docker Desktop and wait until the engine is green/running.
  - Re-run `docker version` and confirm `Server` appears.
- PostgreSQL fails with `Bind for 0.0.0.0:5432 failed: port is already allocated`:
  - Another process (often a local PostgreSQL service) already uses port `5432`.
  - Find what owns the port in PowerShell:

```powershell
netstat -ano | findstr :5432
```

  - Note the PID in the last column, then check process name:

```powershell
tasklist /FI "PID eq <PID>"
```

  - If it is a local PostgreSQL/service you do not need right now, stop it and retry:

```powershell
Stop-Service -Name postgresql* -ErrorAction SilentlyContinue
docker compose up -d
```

  - If you want to keep your local PostgreSQL running, map Docker PostgreSQL to another host port:
  1. Edit `docker-compose.yml` PostgreSQL ports from `"5432:5432"` to `"5433:5432"`.
  2. Edit `src/main/resources/application.properties` database URL from `localhost:5432` to `localhost:5433`.
  3. Run `docker compose up -d` again.
- App starts but login fails:
  - Confirm you started Spring with profile `local` so `data-local.sql` is loaded.
