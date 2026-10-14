// Public configuration only. Never put credentials here.
// Production API origin (public configuration, not a secret).
const local = ['localhost', '127.0.0.1'].includes(location.hostname);
const productionOrigin = 'https://sinnar-mobile-production.up.railway.app';
window.SM_CONFIG = Object.freeze({
  API_BASE_URL: local
    ? (location.port === '5500' ? 'http://localhost:8080' : '')
    : (location.origin === productionOrigin ? '' : productionOrigin),
  // The admin dashboard is served by the backend at /admin/.
  ADMIN_URL: local ? '' : productionOrigin + '/admin/',
});
