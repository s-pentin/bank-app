# Банк (bank-app)

Микросервисное приложение «Банк». Веб-интерфейс позволяет клиенту банка:
- смотреть и редактировать данные своего аккаунта (ФИО, дата рождения);
- пополнять счёт и снимать деньги;
- переводить деньги на счёт другого клиента.

## Архитектура

```
Браузер
   │
   ▼
Front UI :8080 ──OAuth2 Authorization Code + PKCE──▶ Keycloak :8180 (realm "bank")
   │ REST + JWT
   ▼
Gateway :8090 (Spring Cloud Gateway — валидирует JWT, пробрасывает дальше)
   ├─▶ Accounts-service :8081
   ├─▶ Cash-service     :8082
   └─▶ Transfer-service :8083

Accounts/Cash/Transfer ──(M2M, Client Credentials)──▶ Notifications-service :8084

Consul :8500  — Service Discovery + Externalized Config (KV)
PostgreSQL :5433 — одна СУБД, схема на каждый сервис
```

### Сервисы

| Сервис | Порт | Схема БД | Назначение |
|---|---|---|---|
| `front` | 8080 | — | Thymeleaf UI, OAuth2-login, запросы в Gateway |
| `gateway` | 8090 | — | Spring Cloud Gateway, валидация JWT, маршрутизация |
| `accounts-service` | 8081 | `accounts` | аккаунты и балансы (единая точка правды о деньгах) |
| `cash-service` | 8082 | `cash` | пополнение/снятие (RPI → Accounts) |
| `transfer-service` | 8083 | `transfer` | перевод между счетами (RPI → Accounts) |
| `notifications-service` | 8084 | `notifications` | журнал уведомлений |

### Паттерны микросервисной архитектуры

- **Gateway API** — единая точка входа (Spring Cloud Gateway MVC).
- **Service Discovery** — регистрация сервисов в Consul, вызовы по логическому имени (`lb://`).
- **Externalized / Distributed Config** — общие настройки в Consul KV (`spring.config.import=optional:consul:`).
- **Access Token** — пользовательский JWT (Authorization Code) пробрасывается через Gateway; межсервисные вызовы — M2M JWT (Client Credentials).
- **RPI (Remote Procedure Invocation)** — синхронные REST-вызовы между сервисами (`RestClient` + `@LoadBalanced`).
- **Circuit Breaker** — Resilience4j на вызовах Cash/Transfer → Accounts.
- **Transactional Outbox** — запись бизнес-операции и намерения уведомить в одной транзакции; фоновый `OutboxPoller` доставляет в Notifications (облегчённая версия без брокера).
- **Schema per Service** — один общий PostgreSQL, отдельная схема на сервис.
- **UI Composition** — фронт собирает данные из Accounts/Cash/Transfer на одной Thymeleaf-странице.

## Технологический стек

| Слой | Технология |
|---|---|
| Язык | Java 21 |
| Фреймворк | Spring Boot 3.3.6, Spring Cloud 2023.0.6 |
| Сборка | Gradle (мультимодуль), Kotlin DSL |
| Web | Spring Web MVC + Thymeleaf |
| Данные | Spring Data JPA + Hibernate, Flyway |
| БД | PostgreSQL 16 |
| Service Discovery / Config | Consul 1.19 |
| Авторизация | Keycloak 26.0 (OAuth 2.0 / OpenID Connect) |
| Устойчивость | Resilience4j (Circuit Breaker) |
| Тесты | JUnit 5, Mockito, Spring Boot Test, Testcontainers |
| Контейнеризация | Docker, Docker Compose |

## Структура проекта (мультимодуль)

```
bank-app/
├── front/                  # UI + OAuth2-login
├── gateway/                # API Gateway
├── accounts-service/       # аккаунты/балансы
├── cash-service/           # пополнение/снятие
├── transfer-service/       # переводы
├── notifications-service/  # уведомления
├── keycloak/realm-export.json   # realm "bank" (клиенты, роли, пользователи)
├── consul/config/application.json # общие настройки для Consul KV
├── init-schemas.sql        # создание схем БД
├── docker-compose.yml
└── build.gradle.kts / settings.gradle.kts
```

## Запуск

### Предварительные требования

- **Docker** (и Docker Compose) — для запуска всего стека.
- **JDK 21** — для локальной сборки/запуска.

### Запуск в Docker (весь стек одной командой)

```bash
cp .env.example .env    # заполнить реальные секреты (см. ниже)
docker compose up --build
```

Поднимутся 10 сервисов: `postgres`, `consul`, `consul-config-import` (одноразовый импорт `consul/config/application.json` в KV), `keycloak`, 4 бизнес-сервиса, `gateway`, `front`.

После старта откройте **http://localhost:8080** — произойдёт редирект на Keycloak, войдите под тестовым пользователем.

> **Для логина из браузера** нужно, чтобы имя `keycloak` резолвилось на хост. Добавьте в `/etc/hosts`:
> ```
> 127.0.0.1 keycloak
> ```

Полезные команды:

```bash
docker compose ps                 # статус контейнеров
docker compose logs -f front      # логи фронта
docker compose down               # остановить
```

### Запуск локально (для разработки)

1. Поднимите инфраструктуру (БД, Consul, Keycloak) в Docker:

   ```bash
   docker compose up -d postgres consul keycloak
   ```

2. Запустите сервисы (порядок: Accounts → Notifications → Cash/Transfer → Gateway → Front):

   ```bash
   ./gradlew :accounts-service:bootRun
   # в других терминалах:
   ./gradlew :notifications-service:bootRun
   ./gradlew :cash-service:bootRun
   ./gradlew :transfer-service:bootRun
   ./gradlew :gateway:bootRun
   ./gradlew :front:bootRun
   ```

3. Откройте **http://localhost:8080**.

Локальные сервисы подключаются к инфраструктуре в Docker по адресам:
- PostgreSQL: `localhost:5433` (хост-порт Docker; внутри сети — `postgres:5432`, задаётся через `DB_PORT`);
- Consul: `localhost:8500` (`CONSUL_HOST`);
- Keycloak: `http://keycloak:8180/realms/bank` (`KEYCLOAK_ISSUER_URI`; нужен `127.0.0.1 keycloak` в `/etc/hosts`).

## Переменные окружения (`.env`)

| Переменная | Назначение |
|---|---|
| `DB_USERNAME` / `DB_PASSWORD` | учётка PostgreSQL |
| `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` | админ Keycloak |
| `ACCOUNTS_CLIENT_SECRET` | client secret `accounts-service` |
| `CASH_CLIENT_SECRET` | client secret `cash-service` |
| `TRANSFER_CLIENT_SECRET` | client secret `transfer-service` |

> **Важно:** client secrets в `.env` должны совпадать с `keycloak/realm-export.json`
> (по умолчанию `accounts-service-secret`, `cash-service-secret`, `transfer-service-secret`),
> иначе межсервисная (M2M) авторизация не сработает.

## Пользователи Keycloak (для теста)

| Логин | Пароль |
|---|---|
| `ivan` | `password` |
| `petr` | `password` |
| `olga` | `password` |

## Тестирование

```bash
./gradlew test        # все тесты
./gradlew build       # сборка + тесты
```

Уровни покрытия:
- **Unit** — бизнес-логика (JUnit 5 + Mockito).
- **WebMvcTest** — HTTP-слой и авторизация (`spring-security-test`, `jwt()`).
- **Интеграционные** — полный цикл с реальной БД (Testcontainers + PostgreSQL).

## Известные ограничения

- **Transactional Outbox** реализован в облегчённом виде (без CDC/распределённой блокировки, без брокера сообщений) — синхронный поллер доставляет уведомления.
- **Transfer** использует ручную компенсацию при частичном отказе (возврат средств отправителю), а не полноценную Saga.
- **Consul** работает в режиме `-dev` (single-node, для локальной разработки).
- Контрактные тесты (Spring Cloud Contract) — отдельный этап.