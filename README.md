# Student Hub

Выполнил: Пономарев Вячеслав ТРПО24-4

Полностью готовый локальный запуск состоит из PostgreSQL, Spring Boot API и React-интерфейса.

## Запуск через Docker Compose

Требуется только установленный Docker Desktop:

```bash
cp .env.example .env
# Измените секреты и пароли в .env
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
| `superadmin@example.com` | `superadmin` | Суперадминистратор |
| `admin@example.com` | `admin` | Администратор |
| `teacher@example.com` | `teacher` | Преподаватель |

Пароли указаны для стандартного `.env`. Для рабочего или публичного окружения
обязательно замените их в `.env` и измените `JWT_SECRET`.

## Локальный запуск без Docker

Сначала запустите PostgreSQL и создайте файл окружения из шаблона:

```bash
cp .env.example .env
```

При необходимости измените параметры подключения к PostgreSQL в `.env`.
Для локального запуска значения `SPRING_DATASOURCE_*` должны соответствовать
запущенному PostgreSQL.

Backend:

```bash
cd backend
set -a; source ../.env; set +a
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

В этом режиме фронтенд доступен на `http://localhost:5173`, а Vite проксирует `/api` на backend `http://localhost:8080`.
