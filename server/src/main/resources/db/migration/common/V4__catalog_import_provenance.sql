CREATE TABLE catalog_import (
    id UUID PRIMARY KEY,
    source_name VARCHAR(160) NOT NULL,
    source_hash VARCHAR(128) NOT NULL UNIQUE,
    expected_observation_count INTEGER NOT NULL,
    observed_observation_count INTEGER NOT NULL,
    product_count INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    CHECK (expected_observation_count >= 0),
    CHECK (observed_observation_count >= 0),
    CHECK (product_count >= 0)
);

CREATE TABLE catalog_import_observation (
    id UUID PRIMARY KEY,
    catalog_import_id UUID NOT NULL REFERENCES catalog_import(id) ON DELETE CASCADE,
    source_ordinal INTEGER NOT NULL,
    product_id UUID REFERENCES product(id),
    observation_role VARCHAR(32) NOT NULL,
    observed_price NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    parse_status VARCHAR(32) NOT NULL,
    display_marker VARCHAR(120),
    supplemental_text TEXT,
    error_code VARCHAR(80),
    UNIQUE (catalog_import_id, source_ordinal)
);

CREATE INDEX idx_catalog_import_observation_product ON catalog_import_observation(product_id);
CREATE INDEX idx_catalog_import_observation_status ON catalog_import_observation(parse_status);
