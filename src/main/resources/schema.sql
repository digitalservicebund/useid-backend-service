CREATE TABLE identification_session
(
    id SERIAL PRIMARY KEY,
    useid_session_id uuid NOT NULL,
    refresh_address VARCHAR NOT NULL,
    request_data_groups VARCHAR(255) NOT NULL,
    eid_session_id  uuid
);