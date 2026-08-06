CREATE TABLE brand (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entity_version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE substance (
    id UUID PRIMARY KEY,
    canonical_name VARCHAR(200) NOT NULL UNIQUE,
    description_pt VARCHAR(2000) NOT NULL,
    description_en VARCHAR(2000) NOT NULL,
    efficacy_summary_score INTEGER CHECK (efficacy_summary_score BETWEEN 0 AND 10),
    risk_overall_score INTEGER CHECK (risk_overall_score BETWEEN 0 AND 10),
    evidence_level VARCHAR(32) NOT NULL,
    review_status VARCHAR(32) NOT NULL,
    published_revision INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entity_version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE substance_alias (
    id UUID PRIMARY KEY,
    substance_id UUID NOT NULL REFERENCES substance(id) ON DELETE CASCADE,
    locale VARCHAR(16) NOT NULL,
    alias VARCHAR(200) NOT NULL,
    normalization_source VARCHAR(120) NOT NULL,
    UNIQUE (substance_id, locale, alias)
);

CREATE TABLE tag (
    id UUID PRIMARY KEY,
    slug VARCHAR(120) NOT NULL UNIQUE,
    label_pt VARCHAR(160) NOT NULL,
    label_en VARCHAR(160) NOT NULL,
    tag_group VARCHAR(48) NOT NULL
);

CREATE TABLE substance_tag (
    substance_id UUID NOT NULL REFERENCES substance(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (substance_id, tag_id)
);

CREATE TABLE product (
    id UUID PRIMARY KEY,
    brand_id UUID REFERENCES brand(id),
    original_name VARCHAR(500) NOT NULL,
    store_category VARCHAR(160),
    observed_price NUMERIC(12, 2),
    currency VARCHAR(3),
    listing_url VARCHAR(1000),
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    parse_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entity_version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE product_substance (
    product_id UUID NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    substance_id UUID NOT NULL REFERENCES substance(id),
    order_index INTEGER NOT NULL DEFAULT 0,
    strength_text VARCHAR(200),
    PRIMARY KEY (product_id, substance_id)
);

CREATE TABLE indication (
    id UUID PRIMARY KEY,
    slug VARCHAR(160) NOT NULL UNIQUE,
    label_pt VARCHAR(240) NOT NULL,
    label_en VARCHAR(240) NOT NULL
);

CREATE TABLE efficacy_assessment (
    id UUID PRIMARY KEY,
    substance_id UUID NOT NULL REFERENCES substance(id) ON DELETE CASCADE,
    indication_id UUID NOT NULL REFERENCES indication(id),
    population VARCHAR(500) NOT NULL,
    outcome VARCHAR(500) NOT NULL,
    efficacy_score INTEGER CHECK (efficacy_score BETWEEN 0 AND 10),
    evidence_level VARCHAR(32) NOT NULL,
    rationale_pt VARCHAR(3000) NOT NULL,
    rationale_en VARCHAR(3000) NOT NULL,
    rubric_version VARCHAR(40) NOT NULL,
    review_status VARCHAR(32) NOT NULL,
    publication_revision INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (substance_id, indication_id, population, outcome, publication_revision)
);

CREATE TABLE risk_profile (
    id UUID PRIMARY KEY,
    substance_id UUID NOT NULL UNIQUE REFERENCES substance(id) ON DELETE CASCADE,
    context_pt VARCHAR(1000) NOT NULL,
    context_en VARCHAR(1000) NOT NULL,
    overall_score INTEGER CHECK (overall_score BETWEEN 0 AND 10),
    common_burden INTEGER CHECK (common_burden BETWEEN 0 AND 10),
    severe_acute INTEGER CHECK (severe_acute BETWEEN 0 AND 10),
    chronic_organ INTEGER CHECK (chronic_organ BETWEEN 0 AND 10),
    dependency_score INTEGER CHECK (dependency_score BETWEEN 0 AND 10),
    interaction_score INTEGER CHECK (interaction_score BETWEEN 0 AND 10),
    product_quality INTEGER CHECK (product_quality BETWEEN 0 AND 10),
    regulatory_uncertainty INTEGER CHECK (regulatory_uncertainty BETWEEN 0 AND 10),
    rationale_pt VARCHAR(3000) NOT NULL,
    rationale_en VARCHAR(3000) NOT NULL,
    rubric_version VARCHAR(40) NOT NULL
);

CREATE TABLE source (
    id UUID PRIMARY KEY,
    title VARCHAR(1000) NOT NULL,
    source_type VARCHAR(80) NOT NULL,
    url VARCHAR(1500) NOT NULL UNIQUE,
    jurisdiction VARCHAR(120),
    published_at TIMESTAMP WITH TIME ZONE,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
    content_hash VARCHAR(128) NOT NULL
);

CREATE TABLE evidence_claim (
    id UUID PRIMARY KEY,
    substance_id UUID NOT NULL REFERENCES substance(id) ON DELETE CASCADE,
    claim_pt VARCHAR(3000) NOT NULL,
    claim_en VARCHAR(3000) NOT NULL,
    extract_text VARCHAR(2000),
    publication_revision INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE evidence_claim_source (
    evidence_claim_id UUID NOT NULL REFERENCES evidence_claim(id) ON DELETE CASCADE,
    source_id UUID NOT NULL REFERENCES source(id),
    PRIMARY KEY (evidence_claim_id, source_id)
);

CREATE TABLE adverse_effect (
    id UUID PRIMARY KEY,
    substance_id UUID NOT NULL REFERENCES substance(id) ON DELETE CASCADE,
    name_pt VARCHAR(300) NOT NULL,
    name_en VARCHAR(300) NOT NULL,
    frequency_class VARCHAR(80),
    severity_class VARCHAR(80) NOT NULL,
    evidence_claim_id UUID REFERENCES evidence_claim(id)
);

CREATE TABLE regulatory_status (
    id UUID PRIMARY KEY,
    substance_id UUID NOT NULL REFERENCES substance(id) ON DELETE CASCADE,
    jurisdiction VARCHAR(120) NOT NULL,
    authority VARCHAR(160) NOT NULL,
    status_pt VARCHAR(500) NOT NULL,
    status_en VARCHAR(500) NOT NULL,
    effective_date DATE,
    source_id UUID NOT NULL REFERENCES source(id)
);

CREATE TABLE source_snapshot (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL REFERENCES source(id) ON DELETE CASCADE,
    license_allows_raw BOOLEAN NOT NULL,
    bounded_extract VARCHAR(4000),
    raw_payload TEXT,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK (license_allows_raw OR raw_payload IS NULL)
);

CREATE TABLE research_job (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(120) NOT NULL UNIQUE,
    locale VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    entity_version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE research_job_item (
    id UUID PRIMARY KEY,
    research_job_id UUID NOT NULL REFERENCES research_job(id) ON DELETE CASCADE,
    requested_term VARCHAR(160) NOT NULL,
    normalized_term VARCHAR(200),
    substance_id UUID REFERENCES substance(id),
    status VARCHAR(32) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    error_code VARCHAR(80),
    error_message VARCHAR(1000),
    next_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (research_job_id, requested_term)
);

CREATE TABLE draft_revision (
    id UUID PRIMARY KEY,
    substance_id UUID NOT NULL REFERENCES substance(id),
    research_job_item_id UUID REFERENCES research_job_item(id),
    revision INTEGER NOT NULL,
    review_status VARCHAR(32) NOT NULL,
    payload_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (substance_id, revision)
);

CREATE TABLE review_decision (
    id UUID PRIMARY KEY,
    draft_revision_id UUID NOT NULL UNIQUE REFERENCES draft_revision(id),
    decision VARCHAR(32) NOT NULL,
    reviewer VARCHAR(160) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    decided_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_substance_review_status ON substance(review_status);
CREATE INDEX idx_product_parse_status ON product(parse_status);
CREATE INDEX idx_research_job_status ON research_job(status);
CREATE INDEX idx_research_item_status_retry ON research_job_item(status, next_retry_at);
CREATE INDEX idx_efficacy_substance ON efficacy_assessment(substance_id);
CREATE INDEX idx_claim_substance ON evidence_claim(substance_id);
