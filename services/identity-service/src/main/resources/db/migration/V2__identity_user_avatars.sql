CREATE TABLE IF NOT EXISTS identity.user_avatars (
    user_id VARCHAR(36) NOT NULL PRIMARY KEY,
    image_data BYTEA NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    CONSTRAINT fk_user_avatars_user FOREIGN KEY (user_id) REFERENCES identity.users (id) ON DELETE CASCADE
);
