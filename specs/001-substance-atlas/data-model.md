# Data Model

## Catalog

- `brand`: normalized manufacturer or marketplace brand.
- `product`: original listing name plus store category, observed price/currency, listing URL, capture time, and parse status.
- `substance`: canonical active ingredient or exact combination.
- `substance_alias`: locale-aware synonym and normalization provenance.
- `product_substance`: ordered component relation; optional declared strength text remains provenance, not advice.
- `tag` and `substance_tag`: controlled taxonomy grouped by therapeutic class, mechanism, physiological effect, clinical use, sport context, risk, regulatory state, and evidence state.

## Evidence

- `indication`: canonical condition/use context with localized labels.
- `efficacy_assessment`: nullable 0-10 score, population, outcome, evidence level, rationale, rubric version, review status, and publication revision.
- `risk_profile`: nullable overall pharmacological score plus common-burden, severe-acute, chronic-organ, dependency, interaction, product-quality, and regulatory-uncertainty dimensions.
- `adverse_effect`: localized term, frequency class, severity class, and evidence claim.
- `regulatory_status`: jurisdiction, authority, status, effective date, and source.
- `source`: stable citation metadata, source type, jurisdiction, URL, publication/fetch dates, and content hash.
- `evidence_claim`: atomic claim linked to one or more sources and an assessment revision.
- `source_snapshot`: optional licensed raw payload or bounded extract.

## Workflow

- `research_job`: idempotency key, requested locale, overall status, counts, and timestamps.
- `research_job_item`: normalized term, canonical substance, state, attempts, error code, and next retry.
- `draft_revision`: immutable candidate payload, rubric version, creator, and timestamps.
- `review_decision`: approve/reject decision, reviewer, reason, and resulting publication revision.

All mutable rows use UUID primary keys, optimistic versioning, `created_at`, and `updated_at`. Published assessment revisions are immutable.
