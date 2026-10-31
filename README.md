# Student Hub

Выполнил: Пономарев Вячеслав ТРПО24-4

Полностью готовый локальный запуск состоит из PostgreSQL, Spring Boot API и React-интерфейса.

## Запуск через Docker Compose

Требуется только установленный Docker Desktop:

```bash
docker compose up --build
```

После успешного запуска откройте `http://localhost`.

Остановка:

```bash
docker compose down
```

Остановка с удалением данных PostgreSQL:

```bash
docker compose down -v
```

По умолчанию PostgreSQL хранится в Docker volume `postgres-data`. При первом запуске Spring Boot автоматически создаёт схему, таблицы, тестовых пользователей и демонстрационных студентов.

## Тестовые аккаунты

| Email | Пароль | Роль |
|---|---|---|
| `superadmin@example.com` | `superAdmin` | `SUPER_ADMIN` |
| `admin@example.com` | `admin` | `ADMIN` |
| `teacher@example.com` | `teacher` | `TEACHER` |

## Локальный запуск без Docker

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

В этом режиме фронтенд доступен на `http://localhost:5173`, а Vite проксирует `/api` на backend `http://localhost:8080`.
