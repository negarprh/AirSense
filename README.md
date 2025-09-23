# NASA Space Apps - AirSense

AirSense is a monorepo built for NASA Space Apps. It delivers a Spring Boot backend that normalizes air quality readings and a Vite/React dashboard that turns those readings into actionable, rule-based guidance—no LLMs required.

## Features
- **Rule-driven AQI intelligence** sourced from a mock OpenAQ client with a clear extension point for real APIs.
- **Deterministic health advice** that tightens guidance for asthma-sensitive users.
- **In-memory caching** to avoid duplicate upstream calls for the same city/day.
- **PostgreSQL persistence** for recent AQI readings to support historical insights.
- **Ready-to-run Docker Compose** stack with backend, frontend, and database services.
- **springdoc-openapi** available at `/swagger-ui` for quick API exploration.

## Getting Started
1. Copy the sample environment file:
   ```bash
   cp .env.example .env
   ```
2. Build the containers:
   ```bash
   docker compose build
   ```
3. Launch the stack (Postgres, backend, frontend):
   ```bash
   docker compose up -d
   ```
4. Visit the apps:
   - Frontend UI: http://localhost:5173
   - Backend Swagger UI: http://localhost:8080/swagger-ui
   - API Health Check: http://localhost:8080/api/health

Shut down and remove containers/volumes:
```bash
docker compose down -v
```

Tail backend logs:
```bash
docker compose logs -f backend
```

Run backend unit tests inside the container:
```bash
docker compose exec backend ./mvnw -q -DskipITs test
```

## Architecture Notes
- **Backend** (`backend/`): Spring Boot 3 (Java 21), Spring Web, Spring Data JPA, Validation, PostgreSQL driver, springdoc-openapi. AQI data is mocked via a deterministic generator and cached in-memory per `(city, date)`.
- **Frontend** (`frontend/`): Vite + React + TypeScript single page app with reusable components for search, AQI visualization, and guidance.
- **Database**: PostgreSQL 16 container with persistent volume `aqi_pg`.
- **Caching**: `SimpleCache` provides TTL-based in-memory storage for AQI responses.
- **Docker**: Multi-stage backend image and lightweight Node dev container for Vite; orchestrated via `docker-compose.yml`.

## API Overview
- `GET /api/health` → `{ "status": "ok" }`
- `GET /api/aqi?city={CityName}` → Returns normalized AQI payload with 24-hour history and metadata.
- `GET /api/advice?city={CityName}&asthma=true|false` → Returns rule-based advice, automatically stricter for asthma-sensitive users.
- `GET /actuator/health` → Spring Boot actuator health endpoint.

## Testing
- `HealthApiTest` verifies the custom health endpoint.
- `AdviceServiceTest` ensures rule-band logic (including asthma adjustment) behaves deterministically.

## NEXT STEPS
1. Replace the mock OpenAQ client with real API calls.
2. Add Earthdata/TEMPO integration when credentials are available.
3. Optional: add i18n strings (EN/FR) for advice texts.
4. Deploy to AWS (Elastic Beanstalk for backend, RDS for DB, S3 or Amplify for frontend).
