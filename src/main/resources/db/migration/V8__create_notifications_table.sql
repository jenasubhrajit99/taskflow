CREATE TYPE notification_type AS ENUM (
    'TASK_ASSIGNED',
    'TASK_STATUS_CHANGED',
    'TASK_COMMENTED',
    'TASK_DUE_SOON',
    'PROJECT_MEMBER_ADDED',
    'WORKSPACE_MEMBER_ADDED',
    'MENTION'
);

CREATE TABLE notifications (
    id                  UUID              PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_id        UUID              NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type                notification_type NOT NULL,
    title               VARCHAR(255)      NOT NULL,
    message             TEXT              NOT NULL,
    read                BOOLEAN           NOT NULL DEFAULT FALSE,
    related_entity_id   VARCHAR(36),
    related_entity_type VARCHAR(50),
    created_at          TIMESTAMP         NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255)
);

CREATE INDEX idx_notifications_recipient     ON notifications(recipient_id);
CREATE INDEX idx_notifications_unread        ON notifications(recipient_id, read) WHERE read = FALSE;
CREATE INDEX idx_notifications_type          ON notifications(type);
