ALTER TABLE identification_session ADD tenant_id VARCHAR(50) NOT NULL DEFAULT 'Grundsteuer';
ALTER TABLE identification_session ALTER COLUMN tenant_id DROP DEFAULT;