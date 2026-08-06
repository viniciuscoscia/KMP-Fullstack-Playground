# ADR 0011: PostgreSQL is the Substance Atlas source of truth

Status: accepted

Use PostgreSQL 18 with Flyway and JPA for catalog, evidence, revision, and job state. JSONB supports bounded source metadata while normalized relations retain integrity. Full-text search and `pg_trgm` cover v1 discovery. Do not enable pgvector until embeddings and a measured RAG requirement exist. MongoDB, Neo4j, SQLite, and search engines remain possible secondary adapters, not primary stores.
