# Verification record — 14 October 2026

## Executed successfully

- The existing repository was inspected and its original Git history and JPEG photos were preserved.
- Maven compiled the backend with `--release 21` using the available JDK 22.0.2. JDK 21 is configured for the container and CI; that exact runtime has not been executed locally.
- **22 integration tests passed against actual MySQL 8.0.41**, in a newly initialized isolated workspace instance. Flyway applied both migrations and Hibernate validated the real MySQL schema. The user’s installed MySQL service/database was not used or altered.
- **22 integration tests passed against H2 in MySQL mode** in the final Maven wrapper `verify` run. The executable Spring Boot JAR was packaged successfully at `backend/target/sinnar-mobile-1.0.0.jar`.
- Real HTTP checks through `scripts/http_smoke.mjs` passed against the running Spring Boot server backed by MySQL. These used the actual frontend Fetch client, not a mocked API.
- The HTTP checks covered all public HTML pages and `/admin/`, public settings/categories/FAQs, saved contact and repair submissions, invalid form rejection, denied anonymous admin access, login/session/CSRF renewal, authenticated product create/update/delete, search/pagination, image upload and retrieval, enquiry status/deletion, allowed-origin CORS and logout/session denial.
- `python scripts/check_frontend.py` passed for **nine HTML pages**: local links and assets, unique IDs, labels, image alt text, map titles, page metadata, structured data, footer copy and JavaScript syntax.
- Existing images were converted to local WebP without overwriting source photos. No third-party stock photo links are used.

## Issues found and fixed

- Corrected the old phone number everywhere in generated public content and replaced unsupported fixed repair-time/warranty claims with enquiry-based language.
- Fixed explicit Spring Security CORS configuration after a preflight test failed.
- Added proper 404/405 and missing-input handling instead of allowing those cases to return generic 500 responses.
- Removed a singleton CHECK constraint that MySQL rejects on an auto-increment column.
- Corrected the Maven wrapper to use its official packaged script-only distribution.
- Added the backend `/admin/` directory mapping so that the dashboard loads without an `index.html` suffix.
- Used port 9080 for the isolated QA preview after finding that port 8080 was already occupied; the existing listener was left untouched.

## Remaining verification

The browser automation tool reported no available browsers. Therefore browser rendering, screenshots, responsive overflow at 320/375/425/768/1024/1440/1920 px, mobile-menu interaction, keyboard navigation, dashboard dialog interaction and browser console errors **have not been verified in a real browser**. Responsive breakpoints and accessible controls are implemented, but visual correctness is not asserted as tested.

The original live GitHub Pages URL could not be opened by the web tool; analysis used the local repository content. Production deployment, HTTPS/cookie behaviour behind a real reverse proxy, Google Maps pin accuracy, production upload persistence and backup/restore have not been tested. Docker is not installed here, so the supplied container and Compose configuration have not been run.

The existing repository contains no photograph licence evidence. Confirm ownership/licences before public release. Business hours, product prices, availability and promotions need administrator-entered real data before launch.

No live site was published and no production database credentials or accounts were requested or invented. Test-only credentials appear solely in test configuration/QA helpers and are never seeded into production.
