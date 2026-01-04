CREATE TABLE categories (
                            id            UUID                        NOT NULL,
                            user_id       UUID                        NOT NULL,
                            parent_id     UUID,
                            name          VARCHAR(50)                 NOT NULL,
                            description   VARCHAR(255),
                            category_type VARCHAR(20)                 NOT NULL,
                            color         VARCHAR(20)                 NOT NULL,
                            icon          VARCHAR(50),
                            is_active     BOOLEAN                     NOT NULL DEFAULT TRUE,
                            created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                            updated_at    TIMESTAMP WITHOUT TIME ZONE,

                            CONSTRAINT pk_categories PRIMARY KEY (id),
                            CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                            CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id) ON DELETE SET NULL
);