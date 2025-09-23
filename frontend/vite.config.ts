import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "VITE_");
  const apiBase = env.VITE_API_BASE || "http://localhost:8080";

  return {
    plugins: [react()],
    server: {
      host: "0.0.0.0",
      port: Number(env.VITE_PORT || 5173),
      proxy: {
        "/api": {
          target: apiBase,
          changeOrigin: true
        }
      }
    }
  };
});
