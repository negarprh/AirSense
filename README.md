# 🌍 AirSense - Predicting Cleaner, Safer Skies

**Built for the NASA Space Apps Challenge 2025: “From EarthData to Action - Cloud Computing with Earth Observation Data”** 🚀

AirSense is a **web-based air quality forecasting platform** designed for the **NASA Space Apps Challenge 2025**.
It combines **real-time air quality**, **weather**, and **forecast data** to help users stay informed about pollution levels and protect public health.
Inspired by NASA’s **TEMPO mission**, AirSense demonstrates how **Earth observation + cloud computing** can work together to build cleaner, safer skies.

---

## 📡 Live Demo

🌐 **Live Link:** [airsenseapp.org](https://airsenseapp.org)

---

## 🎥 Demo Video

🎬 **Watch the Demo:** ![AirSense Demo](./demo/demo.gif)

*Showing live AQI monitoring, forecast trends, and city-level analytics powered by OpenWeather & AWS Lambda.*

---

## 🖼️ Screenshots

| City Dashboard                  | 4-Day Forecast Visualization            |
| ------------------------------- | --------------------------------------- |
| ![Home](./screenshots/home.png) | ![Forecast](./screenshots/forecast.png) |

---

## 🚀 Tech Stack

**Frontend**

* React + TypeScript + Vite
* Recharts (for visualization)
* Axios + Framer Motion

**Backend**

* Spring Boot (Java 21)
* RESTful API + Swagger Docs
* Caffeine in-memory caching

**Infrastructure**

* AWS Lambda (serverless backend)
* AWS S3 + CloudFront (secure frontend hosting + HTTPS via ACM)
* Docker for local development

---

## 🌦️ Features

| Category                              | Description                                                                  |
| ------------------------------------- | ---------------------------------------------------------------------------- |
| 🌬 **Live AQI Data**                  | Retrieves current air quality using OpenWeather & ground station data.       |
| 🔮 **3-Day Forecast**                 | Predictive hourly air quality forecasting based on real meteorological data. |
| ☁️ **Serverless Cloud Architecture**  | Built using AWS Lambda, CloudFront, and S3 for scalable cloud computing.     |
| ⚙️ **Smart Caching**                  | Reduces redundant API calls with Spring Caffeine caching.                    |
| 💡 **Interactive Visualizations**     | Dynamic charts for pollution trends and forecasts.                           |
| 🛰️ **Aligned with NASA TEMPO Goals** | Encourages clean-air decision-making via public data integration.            |

---

## 🧠 Challenge Alignment — NASA “From EarthData to Action”

AirSense directly addresses NASA’s **Challenge #15: From EarthData to Action** by demonstrating:
✅ **Cloud Computing Integration:** Runs serverlessly on AWS (Lambda + CloudFront), scaling automatically.
✅ **Earth Observation Data Fusion:** Uses OpenWeather (ground + satellite fusion) for air quality + forecast data.
✅ **Public Health Awareness:** Provides easy-to-understand AQI visuals and personalized city-based results.
✅ **Scalable Web Architecture:** Delivers near real-time environmental data with responsive visualizations.

> 🛰️ *Inspired by NASA’s TEMPO satellite mission — monitoring North American air pollution from space.*

---

## 🧱 Architecture

```
Frontend (React + Vite + CloudFront)
        │
        ▼
Backend API (Spring Boot → AWS Lambda)
        │
        ├── OpenWeather API (Live AQI + Forecast)
        ├── Optional OpenAQ integration
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

* Frontend → [http://localhost:5173](http://localhost:5173)
* Backend → [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧩 API Endpoints

| Endpoint                    | Method | Description                 |
| --------------------------- | ------ | --------------------------- |
| `/api/aqi?city={name}`      | GET    | Current AQI data for a city |
| `/api/forecast?city={name}` | GET    | 4-day air quality forecast  |
| `/swagger-ui/index.html`    | —      | API documentation           |

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

* **Backend:** Packaged JAR → AWS Lambda (serverless runtime).
* **Frontend:** React build → hosted on S3 + distributed via CloudFront (HTTPS).
* **SSL:** Managed by AWS Certificate Manager.
* **Domain:** Optional Route 53 for `https://airsenseapp.org`.

---

## 🔒 Environment Variables

| Variable              | Description                 |
| --------------------- | --------------------------- |
| `OPENWEATHER_API_KEY` | OpenWeather API token       |
| `SERVER_PORT`         | Backend port (default 8080) |
| `CACHE_TTL_MINUTES`   | Optional cache duration     |

---

## 🏆 Hackathon Context

**Challenge:** *NASA Space Apps 2025 - From EarthData to Action: Predicting Cleaner, Safer Skies*
**Focus:** Air quality forecasting using cloud computing + Earth observation data.
**Goal:** Use cloud-based scalability to transform open-source air pollution data into actionable public health insights.

