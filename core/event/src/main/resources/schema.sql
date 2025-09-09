CREATE TABLE IF NOT EXISTS categories
(
    category_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT category_name_len CHECK (LENGTH(category_name) >= 1 AND LENGTH(category_name) <= 50)
);

CREATE TABLE IF NOT EXISTS events
(
    event_id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_title        VARCHAR(120)  NOT NULL,
    description        VARCHAR(7000) NOT NULL,
    annotation         VARCHAR(2000) NOT NULL,
    initiator_id       BIGINT        NOT NULL,
    category_id        BIGINT        NOT NULL,
    state              INTEGER       NOT NULL,
    created_on         TIMESTAMP WITHOUT TIME ZONE,
    event_date         TIMESTAMP WITHOUT TIME ZONE,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    paid               BOOLEAN       NOT NULL,
    participant_limit  INTEGER       NOT NULL,
    request_moderation BOOLEAN       NOT NULL,
    CONSTRAINT annotation_len CHECK (LENGTH(annotation) >= 20 AND LENGTH(annotation) <= 2000),
    CONSTRAINT description_len CHECK (LENGTH(description) >= 20 AND LENGTH(description) <= 7000),
    CONSTRAINT event_title_len CHECK (LENGTH(event_title) >= 3 AND LENGTH(event_title) <= 120),
    CONSTRAINT created_on_not_null CHECK (created_on <> NULL),
    CONSTRAINT event_date_not_null CHECK (event_date <> NULL)
);

CREATE TABLE IF NOT EXISTS locations
(
    event_id  BIGINT PRIMARY KEY,
    latitude  NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL
);

ALTER TABLE events
    DROP CONSTRAINT IF EXISTS fk_events_category_id;
ALTER TABLE events
    ADD CONSTRAINT fk_events_category_id FOREIGN KEY (category_id)
        REFERENCES categories (category_id);

ALTER TABLE locations
    DROP CONSTRAINT IF EXISTS fk_locations_event_id;
ALTER TABLE locations
    ADD CONSTRAINT fk_locations_event_id FOREIGN KEY (event_id)
        REFERENCES events (event_id) ON DELETE CASCADE;