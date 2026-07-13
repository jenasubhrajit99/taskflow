CREATE TABLE workspaces (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    slug        VARCHAR(100) NOT NULL UNIQUE,
    owner_id    UUID         NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

CREATE TABLE workspace_members (
    id           UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID           NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id      UUID           NOT NULL REFERENCES users(id)      ON DELETE CASCADE,
    role         VARCHAR(20) 	NOT NULL DEFAULT 'MEMBER',
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_workspaces_slug               ON workspaces(slug);
CREATE INDEX idx_workspaces_owner_id           ON workspaces(owner_id);
CREATE INDEX idx_workspace_members_workspace   ON workspace_members(workspace_id);
CREATE INDEX idx_workspace_members_user        ON workspace_members(user_id);
