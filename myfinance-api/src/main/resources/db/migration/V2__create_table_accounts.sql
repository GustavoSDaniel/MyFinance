CREATE TABLE accounts (
                          id              UUID                        NOT NULL,
                          user_id         UUID                        NOT NULL,
                          name            VARCHAR(255)                NOT NULL,
                          description     VARCHAR(255),
                          type            VARCHAR(20)                 NOT NULL,
                          initial_balance NUMERIC(15, 2)              NOT NULL DEFAULT 0,
                          current_balance NUMERIC(15, 2)              NOT NULL DEFAULT 0,
                          is_active       BOOLEAN                     NOT NULL DEFAULT TRUE,
                          created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                          updated_at      TIMESTAMP WITHOUT TIME ZONE,

                          CONSTRAINT pk_accounts PRIMARY KEY (id),
                          CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);