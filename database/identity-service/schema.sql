-- Canonical DDL mirror of identity-service Flyway migrations (V1 + V2).
-- Applied at runtime by Flyway; edit migrations first, then keep this file aligned.

CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE identity.users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    bio TEXT,
    avatar_url VARCHAR(1024)
);

CREATE TABLE identity.user_avatars (
    user_id VARCHAR(36) NOT NULL PRIMARY KEY,
    image_data BYTEA NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    CONSTRAINT fk_user_avatars_user FOREIGN KEY (user_id) REFERENCES identity.users (id) ON DELETE CASCADE
);
