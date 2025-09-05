CREATE TABLE entry
(
    id     UUID     NOT NULL,
    date   date     NOT NULL,
    amount DECIMAL  NOT NULL,
    type   SMALLINT NOT NULL,
    CONSTRAINT pk_entry PRIMARY KEY (id)
);

CREATE TABLE rule
(
    id          UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    type        SMALLINT     NOT NULL,
    amount      DECIMAL      NOT NULL,
    start       date         NOT NULL,
    "end"       date,
    schedule_id UUID         NOT NULL,
    CONSTRAINT pk_rule PRIMARY KEY (id)
);

CREATE TABLE schedule
(
    id           UUID     NOT NULL,
    frequency    SMALLINT NOT NULL,
    internal     INT      NOT NULL,
    day_of_month INT      NOT NULL,
    CONSTRAINT pk_schedule PRIMARY KEY (id)
);

ALTER TABLE rule
    ADD CONSTRAINT FK_RULE_ON_SCHEDULE FOREIGN KEY (schedule_id) REFERENCES schedule (id);