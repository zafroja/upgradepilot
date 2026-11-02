import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev: /api calls go to the gateway, so no CORS setup is needed.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { "/api": "http://localhost:8080" },
  },
});
