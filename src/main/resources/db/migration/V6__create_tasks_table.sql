CREATE TYPE task_status   AS ENUM ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED');
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

CREATE TABLE labels (
    id         UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(50) NOT NULL,
    color      VARCHAR(7)  NOT NULL DEFAULT '#6366f1',
    project_id UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE (project_id, name)
);

CREATE TABLE tasks (
    id             UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    title          VARCHAR(500)  NOT NULL,
    description    TEXT,
    project_id     UUID          NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assignee_id    UUID          REFERENCES users(id) ON DELETE SET NULL,
    reporter_id    UUID          NOT NULL REFERENCES users(id),
    status         task_status   NOT NULL DEFAULT 'TODO',
    priority       task_priority NOT NULL DEFAULT 'MEDIUM',
    due_date       DATE,
    story_points   INTEGER,
    parent_task_id UUID          REFERENCES tasks(id) ON DELETE CASCADE,
    created_at     TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255)
);

CREATE TABLE task_labels (
    task_id  UUID NOT NULL REFERENCES tasks(id)  ON DELETE CASCADE,
    label_id UUID NOT NULL REFERENCES labels(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, label_id)
);

CREATE INDEX idx_tasks_project_id     ON tasks(project_id);
CREATE INDEX idx_tasks_assignee_id    ON tasks(assignee_id);
CREATE INDEX idx_tasks_reporter_id    ON tasks(reporter_id);
CREATE INDEX idx_tasks_status         ON tasks(status);
CREATE INDEX idx_tasks_priority       ON tasks(priority);
CREATE INDEX idx_tasks_parent_task_id ON tasks(parent_task_id);
CREATE INDEX idx_labels_project_id    ON labels(project_id);
CREATE INDEX idx_tasks_title_trgm     ON tasks USING gin(title gin_trgm_ops);
