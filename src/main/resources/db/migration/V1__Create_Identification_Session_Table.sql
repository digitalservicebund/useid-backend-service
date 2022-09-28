CREATE TABLE identification_session
(
    id SERIAL PRIMARY KEY,
    useid_session_id UUID NOT NULL UNIQUE,
    refresh_address VARCHAR NOT NULL,
    request_data_groups VARCHAR(5)[] NOT NULL,
    eid_session_id  UUID UNIQUE
);