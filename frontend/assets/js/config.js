// Public configuration only. Never put credentials here.
// Production: set API_BASE_URL to the HTTPS backend origin (without /api).
const local = ['localhost', '127.0.0.1'].includes(location.hostname);
window.SM_CONFIG = Object.freeze({
  API_BASE_URL: local && location.port === '5500' ? 'http://localhost:8080' : '',
  // The admin dashboard is served by the backend at /admin/.
  ADMIN_URL: '',
});
