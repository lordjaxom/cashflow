ALTER TABLE entry
    ADD locked BOOLEAN DEFAULT FALSE NOT NULL;

CREATE TABLE bucket
(
    id   UUID         NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_bucket PRIMARY KEY (id),
    CONSTRAINT uk_bucket_name UNIQUE (name)
);

CREATE TABLE bucket_transaction
(
    id                  UUID           NOT NULL,
    bucket_id           UUID           NOT NULL,
    date                date           NOT NULL,
    amount              DECIMAL(18, 2) NOT NULL,
    type                VARCHAR(255)   NOT NULL,
    projection_entry_id UUID           NOT NULL,
    CONSTRAINT pk_bucket_transaction PRIMARY KEY (id)
);

ALTER TABLE bucket_transaction
    ADD CONSTRAINT fk_bucket_transaction_on_bucket FOREIGN KEY (bucket_id) REFERENCES bucket (id);
