-- comment_likes was defined in V1 but may be absent if V1 was applied from an
-- older version of the file. This migration creates it idempotently.
CREATE TABLE IF NOT EXISTS comment_likes (
    user_id     INT NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    comment_id  INT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, comment_id)
);
