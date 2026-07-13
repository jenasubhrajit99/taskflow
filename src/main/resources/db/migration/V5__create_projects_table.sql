CREATE TABLE projects (
    id           UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    name         VARCHAR(200)   NOT NULL,
    description  TEXT,
    project_key  VARCHAR(10)    NOT NULL,
    workspace_id UUID           NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    owner_id     UUID           NOT NULL REFERENCES users(id),
    status       VARCHAR(20)	NOT NULL DEFAULT 'PLANNING',
    start_date   DATE,
    due_date     DATE,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),
    UNIQUE (workspace_id, project_key)
);

CREATE TABLE project_members (
    id         UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id    UUID         NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    role       VARCHAR(20)	NOT NULL DEFAULT 'DEVELOPER',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE (project_id, user_id)
);

CREATE INDEX idx_projects_workspace_id     ON projects(workspace_id);
CREATE INDEX idx_projects_owner_id         ON projects(owner_id);
CREATE INDEX idx_projects_status           ON projects(status);
CREATE INDEX idx_project_members_project   ON project_members(project_id);
CREATE INDEX idx_project_members_user      ON project_members(user_id);
