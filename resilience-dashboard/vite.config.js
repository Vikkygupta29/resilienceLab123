import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],

  server: {
    proxy: {
      "/service/gateway": {
        target: "http://localhost:8080",
        rewrite: (path) =>
          path.replace(/^\/service\/gateway/, ""),
      },

      "/service/order": {
        target: "http://localhost:8081",
        rewrite: (path) =>
          path.replace(/^\/service\/order/, ""),
      },

      "/service/inventory": {
        target: "http://localhost:8083",
        rewrite: (path) =>
          path.replace(/^\/service\/inventory/, ""),
      },

      "/service/payment": {
        target: "http://localhost:8082",
        rewrite: (path) =>
          path.replace(/^\/service\/payment/, ""),
      },

      "/prometheus": {
        target: "http://localhost:9090",
        changeOrigin: true,
        rewrite: (path) =>
          path.replace(/^\/prometheus/, ""),
      },
    },
  },
});