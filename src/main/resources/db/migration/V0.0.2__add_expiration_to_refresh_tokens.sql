ALTER TABLE refresh_tokens
    ADD COLUMN expiration_at TIMESTAMP NOT NULL AFTER token;
