CREATE TABLE balance
(
    id      UUID           NOT NULL,
    "month" date           NOT NULL,
    balance DECIMAL(18, 2) NOT NULL,
    CONSTRAINT pk_balance PRIMARY KEY (id)
);
