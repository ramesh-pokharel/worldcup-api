-- Switch from per-token unique constraint to per-user unique constraint.
-- 6-digit OTP codes are not globally unique, but must be unique per user at a given time.
ALTER TABLE email_verification_tokens
    DROP CONSTRAINT email_verification_tokens_token_key;

ALTER TABLE email_verification_tokens
    ADD CONSTRAINT uq_email_tokens_user_token UNIQUE (user_id, token);
