CREATE TABLE invite_tokens (
    token        VARCHAR(255) PRIMARY KEY,
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE
);
