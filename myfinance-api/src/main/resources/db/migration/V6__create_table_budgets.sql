CREATE TABLE budgets (
    id            UUID                        NOT NULL,
    user_id       UUID                        NOT NULL,
    category_id   UUID                        NOT NULL,
    name          VARCHAR(255)                NOT NULL,
    description   VARCHAR(255),
    limit_amount  NUMERIC(15, 2)              NOT NULL,
    spent_amount  NUMERIC(15, 2)              NOT NULL DEFAULT 0,
    start_date    DATE                        NOT NULL,
    end_date      DATE                        NOT NULL,
    is_active     BOOLEAN                     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT pk_budgets PRIMARY KEY (id),
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);