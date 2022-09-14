CREATE TABLE identification_session
(
    useid_session_id VARCHAR(36) PRIMARY KEY,
    refresh_address VARCHAR NOT NULL,
    request_data_groups VARCHAR(255) NOT NULL,
    eid_session_id  VARCHAR(36)
);