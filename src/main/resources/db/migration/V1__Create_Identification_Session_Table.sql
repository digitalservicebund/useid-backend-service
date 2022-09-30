CREATE TABLE identification_session
(
    id SERIAL PRIMARY KEY,
    useid_session_id UUID NOT NULL UNIQUE, -- PostgreSQL automatically creates an index on columns with unique constraint
    refresh_address VARCHAR NOT NULL,
    request_data_groups VARCHAR(5)[] NOT NULL,
    eid_session_id  UUID UNIQUE
);
