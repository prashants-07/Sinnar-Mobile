# Branding, footer and map update — 14 October 2026

## Completed

- Shared `SINNAR MOBILE` wordmark with an original smartphone symbol, electric-blue accent, bold system typography and styles for both light/dark backgrounds. Public header/footer and administrator header use the same markup generator.
- Original local SVG icons for all 17 existing catalogue categories, plus repair/accessories aliases. Unknown administrator-created categories use a package icon. Static and dynamic cards retain their original category links.
- Updated footer keeps shop details, FAQ/privacy/admin links and the exact copyright. It adds all six requested quick links, accessible WhatsApp/Instagram icons and `Created by Clarigence.in` linking directly to `https://clarigence.in/` in a new tab with `noopener noreferrer`.
- Instagram remains visibly non-clickable while `INSTAGRAM_URL` is blank. Its real link appears only after a valid HTTPS Instagram profile URL is configured. There is no `#` placeholder or invented profile.
- Exact requested home/page titles and matching Open Graph titles; `og:site_name` identifies Sinnar Mobile.
- Responsive SVG/PNG/ICO favicons and Apple touch icon, referenced on every public/admin HTML page.
- Focused layout rules for brand/menu spacing at 320 px, consistent category icon wells, two-column tablet footer, single-column mobile footer, wrapped contact details, social targets and centered mobile copyright/credit. Reduced-motion support is retained.

## Exact map location

The owner’s link `https://maps.app.goo.gl/g274oeQUb9x3pte76` resolved to Google’s **SINNAR MOBILE** place listing. Its place-coordinate fields contain `!3d19.8431708!4d73.9948891`. These are the actual place coordinates, not the map viewport centre.

Embedded URL: `https://maps.google.com/maps?q=19.8431708,73.9948891&z=18&output=embed`. It returns a redirect to Google’s Maps embed endpoint, which returned HTTP 200. All directions buttons keep the exact owner-supplied short link. Dynamic shop-setting updates no longer replace this verified location with an approximate address search. Structured data includes these verified coordinates.

The only backend source edit is the Content Security Policy frame allowlist: it additionally allows `https://www.google.com/maps/embed`, the final Google redirect destination. API behaviour, authentication, database, migrations, forms, catalogue filters and admin CRUD logic are unchanged.

## Files

Modified source:

- `scripts/build_pages.py`: shared brand/footer/map markup and page metadata; generates the eight public pages and updates only admin branding/favicon markup.
- `frontend/assets/css/style.css`: branding, SVG/category, map and responsive footer rules.
- `frontend/assets/js/app.js`: category SVG rendering, exact map configuration and conditional Instagram link.
- `frontend/assets/js/config.js`: `INSTAGRAM_URL`, `MAPS_DIRECTIONS_URL` and `MAP_EMBED_URL`; existing API/admin origins are retained.
- `.github/workflows/pages.yml`: preserves social/map configuration when inserting a deployment API origin.
- `scripts/check_frontend.py`: permits correctly marked decorative logo images with empty alt text.
- `backend/src/main/java/com/sinnarmobile/SecurityConfig.java`: narrow Google Maps frame allowlist correction.
- Generated public/admin HTML and their corresponding root copies; `favicon.svg` is updated in source and root output.

Created:

- `scripts/branding.py`: reusable branding, footer and SVG definitions.
- `scripts/build_brand_assets.py`: creates icons, logo and antialiased favicon assets using Python’s standard library; no new runtime dependency.
- `frontend/assets/icons/icons.json`, 22 original SVG icon files and matching root copies.
- `frontend/assets/js/icons.js` and root copy.
- `frontend/assets/images/smartphone-logo.svg`, `favicon-16x16.png`, `favicon-32x32.png`, `favicon-192x192.png`, `favicon.ico`, `apple-touch-icon.png` and root copies.
- `scripts/check_ui_updates.py`, `scripts/check_ui_icons.mjs` and this report.

## Verification

- `python scripts/check_frontend.py`: passed all nine HTML pages, local links/assets, labels, image alt text, structured data, metadata and JS syntax.
- `python scripts/check_ui_updates.py`: passed exact page titles, shared header/footer logos, developer link attributes, WhatsApp/directions links, pending Instagram behaviour, map coordinates, 17 static category cards, SVG structure, PNG CRC/dimensions/decompression, ICO entries and generated-root parity.
- `node scripts/check_ui_icons.mjs`: passed requested dynamic category mappings, package fallback, consistent strokes and safe handling of unknown category text.
- The 192 px raster favicon was visually inspected and matches the intended smartphone symbol.
- Owner’s Maps link, final map embed and Clarigence.in returned HTTP 200 when redirects were followed.
- Production read-only checks passed for products, categories (17), shop settings, approved GitHub Pages CORS and anonymous admin rejection (401). No customer data, passwords or production records were modified.
- Maven ran all 26 backend integration/recovery tests successfully against the isolated H2 test database after the frame-policy correction. Existing authentication, CSRF, CRUD, uploads, validation and session tests passed. The production database was not used for this suite.

The browser tool reports **no available browsers**. Therefore visual viewport testing at 320, 375, 425, 768, 1024, 1440 and 1920 px, browser console checks, menu/dialog interaction, and cross-browser favicon rendering remain unverified. CSS includes those responsive layouts, but they are not reported as browser-tested.

## Instagram configuration and regeneration

Set the actual profile in `frontend/assets/js/config.js`, for example by replacing the empty `INSTAGRAM_URL` value with the URL supplied by the shop owner. Do not invent a profile. Then run:

```text
python scripts/build_brand_assets.py
python scripts/build_pages.py
python scripts/publish_static.py
python scripts/check_frontend.py
python scripts/check_ui_updates.py
node scripts/check_ui_icons.mjs
```

The configuration check accepts a blank Instagram setting or a valid HTTPS Instagram profile. The GitHub Pages workflow retains the Instagram/map settings. Asset generation requires only Python, and syntax checks require Node.
