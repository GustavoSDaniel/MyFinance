CREATE TABLE transactions (
                              id              UUID                        NOT NULL,
                              idempotency_key UUID                        NOT NULL,
                              user_id         UUID                        NOT NULL,
                              account_id      UUID                        NOT NULL,
                              category_id     UUID                        NOT NULL,
                              description     VARCHAR(255),
                              amount          NUMERIC(15, 2)              NOT NULL,
                              type            VARCHAR(20)                 NOT NULL,
                              status          VARCHAR(20)                 NOT NULL,
                              time            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                              is_recurring    BOOLEAN                     NOT NULL DEFAULT FALSE,
                              recurrence_type VARCHAR(20),
                              created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                              updated_at      TIMESTAMP WITHOUT TIME ZONE,

                              CONSTRAINT pk_transactions PRIMARY KEY (id),
                              CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                              CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
                              CONSTRAINT fk_transactions_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);