CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_substance_name_trgm
    ON substance USING gin (canonical_name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_product_name_trgm
    ON product USING gin (original_name gin_trgm_ops);
