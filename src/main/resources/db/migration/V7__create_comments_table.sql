CREATE TABLE comments (
    id                UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    content           TEXT      NOT NULL,
    task_id           UUID      NOT NULL REFERENCES tasks(id)    ON DELETE CASCADE,
    author_id         UUID      NOT NULL REFERENCES users(id),
    parent_comment_id UUID      REFERENCES comments(id)          ON DELETE CASCADE,
    edited            BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255)
);

CREATE INDEX idx_comments_task_id           ON comments(task_id);
CREATE INDEX idx_comments_author_id         ON comments(author_id);
CREATE INDEX idx_comments_parent_comment_id ON comments(parent_comment_id);
