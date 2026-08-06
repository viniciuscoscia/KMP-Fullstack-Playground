# Substance Atlas Feature Specification

## Goal

Provide a bilingual evidence workbench that lets a reviewer search, filter, compare, research, and approve structured information about substances and marketplace products across Android, Desktop, Web, and native SwiftUI.

## Primary users

- A reviewer curating evidence and regulatory information.
- A reader comparing efficacy, risk, evidence quality, and uncertainty.
- An external MCP client querying approved data or requesting a draft research job.

## Functional requirements

- Show dashboard coverage, evidence distribution, research status, and high-risk counts.
- Search products and normalized substances with paging, tag, evidence, risk, and review filters.
- Show localized descriptions, aliases, tags, indications, risks, adverse effects, regulatory status, evidence claims, and product provenance.
- Compare 2-5 substances for one indication without collapsing missing evidence into zero.
- Accept one or up to 100 research terms under an idempotency key and expose durable per-item status.
- Require authenticated human approval or rejection before a draft becomes published.
- Export an analytics workbook from the approved database state when a local template is available.
- Expose read tools and draft-only research requests through MCP Streamable HTTP.

## Medical rubric

`efficacyScore` is nullable and belongs to one indication, population, and outcome. Values mean: 0 no meaningful benefit; 1-2 minimal or inconsistent; 3-4 small; 5-6 moderate; 7-8 substantial; 9-10 strong and repeatedly demonstrated. `null` means insufficient direct evidence.

`riskScore` is nullable and belongs to a declared route/population/use context. Values mean: 0 minimal; 1-2 mild; 3-4 relevant but generally manageable; 5-6 significant; 7-8 serious; 9-10 extreme. Pharmacological risk, product-quality risk, and regulatory uncertainty remain separate fields.

Evidence quality is independent of score: `HIGH`, `MODERATE`, `LOW`, `VERY_LOW`, or `INSUFFICIENT`. Every non-null score requires a versioned rubric, rationale, reviewer, and at least one source.

Exact blends receive a combined efficacy assessment only when that exact combination was studied. Otherwise the UI shows component assessments and an interaction-uncertainty notice.

## Safety requirements

- Never display personalized advice, doses, cycles, stacks, sourcing guidance, or purchase ranking.
- Marketplace price, category, URL, and capture date appear only as stale provenance.
- The server never fetches a caller-provided arbitrary URL.
- Mutation and MCP endpoints require `LOCAL_ADMIN_TOKEN`; secrets and raw source payloads are redacted from logs.
- Raw source payloads are stored only when licensing and terms permit it.

## Acceptance criteria

- Synthetic fixtures render complete dashboard, catalog, detail, compare, queue, and source screens in both locales.
- Research retries, partial failure, ambiguity, idempotency, approval, rejection, and revision preservation are tested.
- Desktop/Web, Android, and iOS layouts remain usable at compact and expanded widths with keyboard and screen-reader semantics.
- Docker Compose starts PostgreSQL, server, and web; `GET /actuator/health` reports healthy and the runtime web URL loads the workbench. On OrbStack this is `http://substance-atlas-web-1.orb.local:8081`; direct host-port runtimes can use the configured `SUBSTANCE_ATLAS_WEB_PORT`.
