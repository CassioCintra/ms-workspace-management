CREATE TABLE workspace_members (
    user_id   VARCHAR(255) NOT NULL PRIMARY KEY,
    role      VARCHAR(50)  NOT NULL,
    joined_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE invites (
    id         UUID         PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    status     VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE api_tokens (
    id           UUID         PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    token_hash   VARCHAR(255) NOT NULL UNIQUE,
    last_used_at TIMESTAMP,
    revoked_at   TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
