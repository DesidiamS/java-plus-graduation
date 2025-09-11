CREATE TABLE IF NOT EXISTS compilations
(
    compilation_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pinned            BOOLEAN     NULL,
    compilation_title VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT compilation_title_len CHECK (LENGTH(compilation_title) >= 1 AND LENGTH(compilation_title) <= 50)
);

CREATE TABLE IF NOT EXISTS events_compilations
(
    event_id       BIGINT,
    compilation_id BIGINT,
    PRIMARY KEY (event_id, compilation_id)
);

ALTER TABLE events_compilations
    DROP CONSTRAINT IF EXISTS fk_events_compilations_compilation_id;
ALTER TABLE events_compilations
    ADD CONSTRAINT fk_events_compilations_compilation_id FOREIGN KEY (compilation_id)
        REFERENCES compilations (compilation_id) ON DELETE CASCADE;