# File changes

Original `index.html` was replaced with the redesigned generated home page. The original 11 JPEG images and `.git` history are preserved. The unused old root `style.css` and `script.js` were removed; their replacements live in `assets/` and `frontend/assets/`.

New public/source files:

- `frontend/index.html`, `about.html`, `products.html`, `offers.html`, `services.html`, `faq.html`, `contact.html`, `privacy.html`
- `frontend/admin/index.html`
- `frontend/assets/css/style.css`, `admin.css`
- `frontend/assets/js/config.js`, `api.js`, `app.js`, `admin.js`
- `frontend/assets/images/Accessories1.webp`, `case1.webp`, `cases.webp`, `covers.webp`, `gift.webp`, `glass.webp`, `iphone1.webp`, `phones.webp`, `reaparing.webp`, `shop.webp`, `watch1.webp`, `favicon.svg`, `README.md`
- `frontend/sitemap.xml`, `robots.txt`
- Root copies of those public pages, `admin/`, `assets/`, `sitemap.xml`, `robots.txt`, plus `.nojekyll`, generated for existing root-based GitHub Pages hosting.

New backend files:

- `backend/pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `Dockerfile`
- `backend/src/main/java/com/sinnarmobile/SinnarMobileApplication.java`
- `Api.java`, `Models.java`, `Repositories.java`, `Requests.java`, `Errors.java`
- `SecurityConfig.java`, `AuthController.java`, `RateLimitFilter.java`
- `CatalogController.java`, `EnquiryController.java`, `UploadController.java`, `AdminPageController.java`, `Bootstrap.java`
- `backend/src/main/resources/application.yml`, `application-local.yml`
- `backend/src/main/resources/db/migration/V1__schema.sql`, `V2__business_content.sql`
- `backend/src/test/java/com/sinnarmobile/ApiIntegrationTest.java`
- `backend/src/test/resources/application-test.yml`

New setup/documentation/automation files:

- `README.md`, `.env.example`, `.gitignore`, `.dockerignore`, `compose.yml`
- `database/setup.sql`, `database/README.md`
- `docs/API.md`, `DEPLOYMENT.md`, `VERIFICATION.md`, `FILES.md`
- `scripts/bootstrap_tools.py`, `optimize_images.py`, `build_pages.py`, `publish_static.py`, `check_frontend.py`, `mysql_qa.py`, `http_smoke.mjs`
- `.github/workflows/verify.yml`, `pages.yml`

Ignored local tooling, Maven caches, test databases, logs, build output and uploads are not deliverable source files. No credentials are committed. Root/source duplication exists only to preserve compatibility with the existing branch/root Pages deployment; the preferred Actions deployment serves `frontend/` directly.
