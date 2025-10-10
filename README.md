# 🌍 AirSense - Live Air Quality & Forecast Platform - NASA App Challenge Hackathon 2025

AirSense delivers live air quality insights, forecasts, and historical analytics for any city. It is built for the NASA App Challenge Hackathon 2025.
It combines **React**, **Spring Boot**, and **OpenWeather APIs** inside a **Dockerized**, cloud-ready architecture deployed on **AWS (App Runner + S3)**.

---

## 🚀 Tech Stack

**Frontend**

* React + TypeScript + Vite
* Recharts (for data visualization)
* Axios + Framer Motion

**Backend**

* Spring Boot (Java 21)
* RESTful API with Swagger Docs
* Caffeine Caching
* PostgreSQL (Neon / Supabase / AWS RDS)
* Spring Actuator for health endpoints

**Infrastructure**

* Docker & Docker Compose
* AWS App Runner (backend)
* AWS S3 + CloudFront (frontend hosting)
* Optional Route 53 custom domain

---

## 🌦️ Features

| Category                 | Description                                                    |
| ------------------------ | -------------------------------------------------------------- |
| 🌬 **Live AQI Data**     | Retrieves current air pollution data from OpenWeather API.     |
| 🔮 **Forecast**          | 4-day hourly forecast using OpenWeather’s predictive endpoint. |
| 📊 **History Tracking**  | Stores and visualizes AQI trends (24 h / 7 d) in PostgreSQL.   |
| ⚙️ **Caching Layer**     | Reduces redundant API calls with in-memory Caffeine cache.     |
| 💡 **API Docs & Health** | Swagger UI + Spring Actuator /health & /metrics endpoints.     |
| ☁️ **Cloud Deployment**  | Fully containerized → AWS App Runner & S3 production ready.    |

---

## 🧱 Architecture

```
Frontend (React, Vite)
        │
        ▼
Backend API (Spring Boot)
        │
        ├── OpenWeather API  ← AQI & forecast data
        ├── PostgreSQL (Neon/Supabase/RDS)
        └── Caffeine Cache
```

---

## 🛠️ Local Development

### Prerequisites

* Node ≥ 18
* Java 21
* Docker Desktop
* Maven or Gradle

### Environment Setup

Create a `.env` file in the root directory:

```bash
BACKEND_PORT=8080
FRONTEND_PORT=5173
OPENWEATHER_API_KEY=your_api_key_here
```

### Run with Docker

```bash
docker compose up --build
```

Then open:
Frontend → [http://localhost:5173](http://localhost:5173)
Backend → [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧩 API Endpoints

| Endpoint                             | Method | Description            |
| ------------------------------------ | ------ | ---------------------- |
| `/api/aqi?city={name}`               | GET    | Current AQI for a city |
| `/api/forecast?city={name}`          | GET    | 4-day forecast         |
| `/api/history?city={name}&hours={n}` | GET    | Historical readings    |
| `/actuator/health`                   | GET    | Health check           |
| `/swagger-ui/index.html`             | —      | Interactive docs       |

---

## 📈 Example Output

```json
{
  "city": "Montreal",
  "pm25": 12.4,
  "aqi": 45,
  "aqi_category": "Good",
  "unit": "µg/m³",
  "observedUtc": "2025-10-09T12:00:00Z",
  "health_advice": "Air quality is satisfactory."
}
```

---

## ☁️ AWS Deployment Overview

* **Backend**: Docker image pushed to ECR → AWS App Runner service.
* **Frontend**: Built React app uploaded to S3 → served via CloudFront.
* **Database**: PostgreSQL (Neon / Supabase / AWS RDS Free Tier).
* **Domain**: Route 53 with SSL via ACM.

---

## 🔒 Environment Variables

| Variable                         | Description                 |
| -------------------------------- | --------------------------- |
| `OPENWEATHER_API_KEY`            | OpenWeather API token       |
| `SERVER_PORT`                    | Backend port (default 8080) |
| `DB_URL` / `DB_USER` / `DB_PASS` | Optional for PostgreSQL     |
| `CACHE_TTL_MINUTES`              | Optional cache duration     |


---

## 🏆 Built For

**NASA Space Apps Challenge 2025 Hackathon**
