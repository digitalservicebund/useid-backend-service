CREATE TABLE transaction_info
(
    id SERIAL PRIMARY KEY,
    useid_session_id UUID NOT NULL UNIQUE,
    provider_name VARCHAR NOT NULL,
    provider_url VARCHAR NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE additional_info
(
    id SERIAL PRIMARY KEY,
    useid_session_id UUID NOT NULL,
    key VARCHAR NOT NULL,
    value VARCHAR NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
