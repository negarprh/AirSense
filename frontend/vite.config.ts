import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "VITE_");

  const apiBase = env.VITE_API_BASE || "http://localhost:8080";
  const port = Number(env.VITE_PORT || 5173);
  // If you map HOST_PORT:5173 and HOST_PORT != 5173, set VITE_HMR_PORT=HOST_PORT
  const hmrClientPort = Number(env.VITE_HMR_PORT || port);

  return {
    plugins: [react()],
    server: {
      host: true,               // 0.0.0.0
      port,                     // keep container port 5173
      strictPort: true,
      watch: { usePolling: true },   // important for Docker file watching
      hmr: { clientPort: hmrClientPort },
      proxy: {
        "/api": {
          target: apiBase,
          changeOrigin: true
        }
      }
    }
  };
});
