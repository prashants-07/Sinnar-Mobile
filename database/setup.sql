-- Run using a MySQL administrator connection. These are placeholders, not real credentials.
-- Use a fresh database. Flyway creates application tables on first backend startup.
CREATE DATABASE IF NOT EXISTS sinnar_mobile CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sinnar_app'@'localhost' IDENTIFIED BY 'REPLACE_WITH_A_STRONG_UNIQUE_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
ON sinnar_mobile.* TO 'sinnar_app'@'localhost';
-- For containers or a managed database, set the account host to the backend network host.
-- Restrict network access to the backend and DBA; never expose MySQL to website visitors.
-- In stricter deployments, use a separate migration account with DDL privileges,
-- then run the application with SELECT/INSERT/UPDATE/DELETE only.
