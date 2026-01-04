CREATE TABLE users (
                       id          UUID                        NOT NULL,
                       name        VARCHAR(255)                NOT NULL,
                       email       VARCHAR(255)                NOT NULL,
                       picture     VARCHAR(500),
                       role        VARCHAR(50)                 NOT NULL,
                       created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                       updated_at  TIMESTAMP WITHOUT TIME ZONE,

                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uk_users_email UNIQUE (email)
);