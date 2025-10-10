// api.ts
import axios from "axios";

/**
 * Resolves the backend base URL dynamically:
 * - Prefers VITE_API_BASE from environment
 * - Fallback: same host on port 8080
 */
function resolveBase(): string {
  const env = (import.meta.env.VITE_API_BASE as string | undefined)?.trim();
  const { protocol, hostname } = window.location;

  if (env) {
    try {
      // Allow relative values like "/api"
      if (env.startsWith("/")) return env;

      const parsed = new URL(env, `${protocol}//${hostname}`);

      // When running via docker-compose the string may be http://backend:8080,
      // but the browser cannot resolve "backend" �?" swap to the current host.
      if (parsed.hostname === "backend" || parsed.hostname === "frontend") {
        parsed.hostname = hostname;
        if (!parsed.port) parsed.port = "8080";
      }

      // Preserve HTTPS if the frontend is served over TLS to avoid mixed content.
      if (protocol === "https:" && parsed.hostname === hostname) {
        parsed.protocol = "https:";
      }

      return parsed.toString();
    } catch {
      // Fall back to default behaviour below.
    }
  }

  return `${protocol}//${hostname}:8080`;
}

export const api = axios.create({
  baseURL: resolveBase(),
  headers: { "Cache-Control": "no-cache" },
});

export type StationCoords = { latitude: number; longitude: number };

export interface AqiResponse {
  city?: string;
  resolved?: string;
  query?: string;
  station?: string | StationCoords;
  stationId?: number;
  sensorId?: number;
  country?: string;
  countryCode?: string;
  locality?: string;
  timezone?: string;
  stationDistanceMeters?: number;
  unit?: string;
  pm25?: number;
  observedUtc?: string;
  aqi?: number;
  aqi_category?: string;
  health_advice?: string;
  message?: string;
}
