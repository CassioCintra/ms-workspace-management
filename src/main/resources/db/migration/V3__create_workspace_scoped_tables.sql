CREATE TABLE workspace_members (
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id      VARCHAR(255) NOT NULL,
    email        VARCHAR(255),
    name         VARCHAR(255),
    role         VARCHAR(50)  NOT NULL,
    joined_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (workspace_id, user_id)
);

CREATE TABLE invites (
    id           UUID         PRIMARY KEY,
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    email        VARCHAR(255) NOT NULL,
    role         VARCHAR(50)  NOT NULL,
    token        VARCHAR(255) NOT NULL UNIQUE,
    expires_at   TIMESTAMP    NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE api_tokens (
    id           UUID         PRIMARY KEY,
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,
    last_used_at TIMESTAMP,
    revoked_at   TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
