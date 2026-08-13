CREATE TABLE app_setting
(
    id            UUID         NOT NULL,
    setting_key   VARCHAR(255) NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    CONSTRAINT pk_app_setting PRIMARY KEY (id),
    CONSTRAINT uk_app_setting_key UNIQUE (setting_key)
);
