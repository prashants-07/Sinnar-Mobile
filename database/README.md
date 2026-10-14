# Database

Canonical, versioned migrations live in `backend/src/main/resources/db/migration/`:

- `V1__schema.sql`: all nine requested tables, foreign keys, indexes, price/status constraints and timestamps.
- `V2__business_content.sql`: 17 categories, ten factual FAQs and the supplied shop details. No production products or offers are fabricated.
- `V3__admin_password_reset.sql`: records an explicitly requested one-time administrator password reset ID. Existing accounts, products and enquiries are preserved.

`setup.sql` provisions an empty MySQL 8 database and application account. Replace its password example locally using a secured DBA connection. Do not run migrations manually and then let Flyway run them again. Do not edit a migration after it has been applied to a shared database; add a new versioned migration instead.

Products refer to categories; offers can optionally refer to products. Deleting a category in use or a product linked to an offer returns HTTP 409. Prices are nullable. Demo products are optional runtime seed content, clearly labelled and priced only by an administrator. Settings are maintained as the single record with ID 1 through the API.

Back up the database and upload storage together. Establish an enquiry retention policy and use the admin delete controls to remove information when it is no longer needed. The application does not automatically expire enquiries.
