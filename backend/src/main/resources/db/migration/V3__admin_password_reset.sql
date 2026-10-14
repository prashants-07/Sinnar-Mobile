-- Record the last explicitly requested reset so ordinary redeploys never reset passwords.
ALTER TABLE admin_users ADD COLUMN password_reset_id VARCHAR(64);
