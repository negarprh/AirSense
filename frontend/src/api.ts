import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || "http://localhost:8080"
});

export interface HistoryPoint {
  timestamp: string;
  aqi: number;
}

export interface AqiResponse {
  city: string;
  aqi: number;
  band: string;
  color: string;
  mainPollutant: string;
  history: HistoryPoint[];
  timestamp: string;
}

export interface AdviceResponse {
  city: string;
  aqi: number;
  band: string;
  publicAdvice: string;
  sensitiveAdvice: string;
  pollutantNote: string;
}
