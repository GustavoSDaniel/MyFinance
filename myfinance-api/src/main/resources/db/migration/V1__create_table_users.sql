CREATE TABLE users (
                       id         UUID                        NOT NULL DEFAULT gen_random_uuid(),
                       email      VARCHAR(255)                NOT NULL,
                       username   VARCHAR(255)                NOT NULL,
                       picture    VARCHAR(255),
                       role       VARCHAR(20)                 NOT NULL DEFAULT 'ROLE_USER',
                       created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP WITHOUT TIME ZONE,
                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uc_users_email UNIQUE (email)
);