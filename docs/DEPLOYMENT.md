# Deployment

The static website and Spring Boot backend are separate deployments. No production credentials or hosting account were supplied, so this implementation does not deploy or alter the live site.

## 1. MySQL 8

Provision a private MySQL 8 instance. Create an empty `sinnar_mobile` database with utf8mb4 and a dedicated account. Allow connections from the backend only. Store `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` in the hosting provider’s environment/secret settings. Enable verified TLS according to the database provider’s instructions, for example `sslMode=VERIFY_IDENTITY` with an appropriate trust store.

Flyway applies the versioned scripts at startup; Hibernate uses `ddl-auto=validate`. For least privilege, run migrations with a separate Flyway account (`SPRING_FLYWAY_USER`/`SPRING_FLYWAY_PASSWORD`) and use only data privileges for the runtime account. Back up MySQL regularly and test a restore before relying on backups.

## 2. Backend

Use a Java 21 host, a container host supporting long-running Java services, or a VPS. Supply a persistent disk for `UPLOAD_DIR`. A stateless host without durable upload storage is unsuitable for the included local filesystem upload implementation.

Build from the repository root:

```powershell
cd backend
.\mvnw.cmd -B -ntp verify
java -jar target/sinnar-mobile-1.0.0.jar
```

Or build the Docker image from the repository root:

```text
docker build -f backend/Dockerfile -t sinnar-mobile .
```

The Docker build copies both backend and frontend source. Tests should pass in CI before image publication; the Docker packaging stage skips running them. Set environment values from `.env.example`, with these production settings:

```text
DB_URL=<private MySQL JDBC URL>
DB_USERNAME=<runtime database user>
DB_PASSWORD=<secret>
ADMIN_USERNAME=<first admin username>
ADMIN_PASSWORD=<12+ characters, maximum 72 UTF-8 bytes>
CORS_ORIGINS=https://prashants-07.github.io
SESSION_SECURE=true
SESSION_SAME_SITE=Lax
UPLOAD_DIR=<persistent upload mount>
SEED_DEMO=false
PORT=8080
```

Terminate HTTPS at a trusted reverse proxy. Forward the host/protocol correctly, block direct public access to the backend’s plain HTTP port, and trust forwarding headers only from that proxy. Configure the proxy’s maximum request size to at least 6 MB and avoid recording enquiry bodies or passwords in logs. The provided Compose file binds the backend to localhost and keeps MySQL off public ports; add your HTTPS proxy separately. Compose requires `MYSQL_ROOT_PASSWORD` in addition to the example environment variables.

Open `https://YOUR-BACKEND-HOST/admin/`, sign in, confirm shop contact details and hours, then add actual products, images and offers. Do not use the test profile in production. The application does not create the test account in a production startup.

Single-instance deployment is supported. For multiple backend instances, add shared session storage (for example Spring Session with Redis), shared persistent upload storage, and distributed edge rate limiting before scaling. The included throttle is per process: 10 login attempts/minute and 5 submissions/minute per remote address. Set edge rate limits as well; if forwarding is misconfigured all visitors can appear as the proxy’s IP.

## 3. GitHub Pages frontend

All pages and assets use relative paths and are compatible with `https://prashants-07.github.io/Sinnar-Mobile/`. Refreshing `products.html`, `offers.html` or any other HTML page works without SPA rewrites.

Preferred workflow:

1. Push the reviewed source to the existing repository.
2. In repository Settings → Pages, choose **GitHub Actions** as the deployment source.
3. In Settings → Secrets and variables → Actions → Variables, add `API_BASE_URL` with the backend’s HTTPS origin, such as `https://api.your-domain.example` (no `/api`, path or trailing slash).
4. Run the **Deploy frontend to GitHub Pages** workflow manually. It validates the HTTPS origin, creates the public `config.js` for that deployment, and uploads only `frontend/`.
5. Confirm the workflow’s returned Pages URL, reload the catalogue and submit a test enquiry, then verify it in the backend dashboard.

Alternative, retaining existing branch/root hosting:

1. Edit `frontend/assets/js/config.js`: set `API_BASE_URL` to the backend HTTPS origin and `ADMIN_URL` to that origin plus `/admin/`.
2. Run `python scripts/publish_static.py`.
3. Push the root output and use the existing branch’s root folder as Pages source. Root files are tracked to support this path; originals and Git history are preserved.

The public API client omits cookies. Admin links go directly to the backend. CORS origins are origins only: use `https://prashants-07.github.io`, not the project path and never `*` with credentials. If a custom frontend domain is added, add its exact HTTPS origin to `CORS_ORIGINS`.

For a custom canonical domain, update `site` in `scripts/build_pages.py`, regenerate the public pages/sitemap, update CORS and sync the root output if applicable. `PUBLIC_SITE_URL` is an operational reference variable; it does not rewrite static canonical URLs automatically.

## 4. Final release checks

Confirm image ownership or replace photographs where rights cannot be confirmed. Add real product listings, current offer terms and verified opening hours. Test 320, 375, 425, 768, 1024, 1440 and 1920 px layouts, navigation, keyboard focus, forms and dashboard editing in a real browser. Check for console errors and overflow. Remove demo products if any were loaded.

Verify HTTPS, allowed/rejected origins, login/logout, saved enquiries, uploaded images after a restart, offer expiry and backups. The Google Map uses the supplied shop address; confirm its pin visually before publication. No public reviews, ratings, opening hours or repair warranties have been invented.
