ALTER TABLE refresh_tokens
    ADD COLUMN expiration_at TIMESTAMP aNOT NULL AFTER token;