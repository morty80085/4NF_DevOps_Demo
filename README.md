# Spring Boot + MySQL CI/CD Demo

This repository contains a minimal Spring Boot demo for CI/CD practice with GitHub Actions and MySQL.

## What is included

- REST API: `GET /api/tasks`, `POST /api/tasks`
- JPA entity + repository (`task_items` table)
- Local profile using MySQL via env variables
- Test profile using in-memory H2 for fast local testing
- CI profile using MySQL service in GitHub Actions
- Manual workflow trigger input for 7 member slots
- Auto DB bootstrap via `schema.sql` and `data.sql`

## Database initialization

- `src/main/resources/schema.sql` creates `task_items` automatically
- `src/main/resources/data.sql` inserts one demo row if table is empty
- MySQL profiles use `spring.jpa.hibernate.ddl-auto=validate`, so schema is controlled by SQL scripts

## Quick Start (local)

1. Start a MySQL instance and create credentials:
   - database: `devops_demo`
   - user: `devops`
   - password: `devops123`
2. Run the app:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## API example

Create a task:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"finish github actions demo"}'
```

List tasks:

```bash
curl http://localhost:8080/api/tasks
```

## Test

Local tests use H2 profile (`test`):

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## GitHub Actions

Workflow file: `.github/workflows/ci-mysql-demo.yml`

- Triggered by `push`, `pull_request`, and `workflow_dispatch`
- `workflow_dispatch` has 7 member slots (`member-1` ... `member-7`)
- CI starts MySQL service and runs Maven tests/build

If needed, replace member slot labels with real GitHub usernames.

