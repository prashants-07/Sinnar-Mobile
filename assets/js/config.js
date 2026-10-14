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
  // Set your actual Instagram profile URL here. Blank keeps the icon non-clickable.
  INSTAGRAM_URL: 'https://www.instagram.com/sinnar_mobile/',
  MAPS_DIRECTIONS_URL: 'https://maps.app.goo.gl/g274oeQUb9x3pte76',
  // Verified place coordinates from the shop owner's shared Google Maps link.
  MAP_EMBED_URL: 'https://maps.google.com/maps?q=19.8431708,73.9948891&z=18&output=embed',
});
