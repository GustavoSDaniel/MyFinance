CREATE TABLE goals (
                       id             UUID                        NOT NULL,
                       user_id        UUID                        NOT NULL,
                       category_id    UUID                        NOT NULL,
                       name           VARCHAR(255)                NOT NULL,
                       description    VARCHAR(255)                NOT NULL,
                       target_amount  NUMERIC(15, 2)              NOT NULL,
                       current_amount NUMERIC(15, 2)              NOT NULL DEFAULT 0,
                       deadline       DATE                        NOT NULL,
                       priority       VARCHAR(20)                 NOT NULL,
                       created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                       updated_at     TIMESTAMP WITHOUT TIME ZONE,

                       CONSTRAINT pk_goals PRIMARY KEY (id),
                       CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                       CONSTRAINT fk_goals_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);