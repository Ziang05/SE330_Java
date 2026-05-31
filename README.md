# Hospital Management System

Base Spring Boot project for a Hospital Management System. This repository is intended as a shared foundation for a 5-person team: entity model, repositories, common API response, exception handling, audit logging, SQL schema/seed data, Docker setup, and one sample Patient module.

## 1. Prerequisites

- Java 17
- Maven 3.9+
- Docker Desktop or Docker Engine with Docker Compose
- MySQL 8.x if running without Docker
- IntelliJ IDEA or another Java IDE

## 2. Clone & Setup `.env`

```bash
copy .env.example .env
```

Adjust values in `.env` if ports or database credentials differ on your machine.

## 3. Run With Docker

```bash
docker compose up -d
```

Docker Compose starts:

- `mysql`: MySQL 8.0 with schema and seed data loaded from `src/main/resources/db`
- `app`: Spring Boot app using the `prod` profile

App URL: `http://localhost:8080`

## 4. Run Local Development

Start a local MySQL database and create/load schema:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS hospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p hospital_db < src/main/resources/db/schema.sql
mysql -u root -p hospital_db < src/main/resources/db/seed_data.sql
```

Run the app:

```bash
mvn spring-boot:run
```

The default profile is `dev`, configured in `src/main/resources/application.yml`. Dev profile uses `ddl-auto=validate`, so the database schema must already exist.

## 5. Swagger UI

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

## 6. Project Structure

- `config`: OpenAPI config and security placeholder
- `entity`: JPA entities and enums
- `repository`: Spring Data JPA repositories
- `service` and `service/impl`: service contracts and implementations
- `controller`: REST controllers
- `dto/request`: request payload classes
- `dto/response`: response payload classes and `ApiResponse`
- `exception`: custom exceptions and global exception handler
- `audit`: `@Auditable` annotation and AOP audit logging
- `util`: small reusable helpers and mappers
- `resources/db`: SQL schema and seed data

## 7. Team Conventions

Branch names:

```text
feature/patient-crud
feature/appointment-schedule
fix/patient-validation
```

Commit format:

```text
feat(patient): add patient search endpoint
fix(invoice): correct paid amount calculation
chore(docker): update mysql healthcheck
```

API responses should use `ApiResponse<T>`:

```java
return ResponseEntity.ok(ApiResponse.ok(data));
return ResponseEntity.ok(ApiResponse.ok("Created successfully", data));
```

Controllers should return DTOs, not JPA entities. Services should own business validation and throw `ResourceNotFoundException`, `DuplicateResourceException`, or `BusinessException` where appropriate.

## Security Note

Spring Security/JWT is intentionally not implemented yet. All endpoints are currently open. See `SecurityPlaceholderConfig` for the TODO marker; later work should add authentication, JWT parsing, role checks, and replace temporary `X-User-Id` audit lookup.

## Sample Endpoints

- `POST /api/v1/patients`
- `GET /api/v1/patients`
- `GET /api/v1/patients/{id}`
- `GET /api/v1/patients/search?keyword=an`
- `PUT /api/v1/patients/{id}`
- `DELETE /api/v1/patients/{id}`
