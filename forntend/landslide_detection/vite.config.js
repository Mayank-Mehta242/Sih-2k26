import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Keep browser requests same-origin during local development and preview.
// This lets every frontend feature use the local Spring backend without CORS.
const BACKEND_URL = 'http://localhost:8080';

const nodesProxy = {
  '/api': {
    target: BACKEND_URL,
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, ''),
  },
};

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: nodesProxy,
  },
  preview: {
    proxy: nodesProxy,
  },
});
