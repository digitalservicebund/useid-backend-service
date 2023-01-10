CREATE TABLE transaction_info
(
    id SERIAL PRIMARY KEY,
    useid_session_id UUID NOT NULL UNIQUE,
    provider_name VARCHAR NOT NULL,
    provider_url VARCHAR NOT NULL,
    additional_information VARCHAR NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
