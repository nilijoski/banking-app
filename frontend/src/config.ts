// API URL will be injected at build time from Render environment variables
// Set VITE_API_URL in Render dashboard environment variables
export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
