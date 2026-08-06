# Project Constitution

## Non-negotiable principles

1. Domain facts are evidence-backed, indication-specific, versioned, and attributable.
2. Unknown efficacy or risk is represented as `null`, never coerced to zero.
3. Research output is a draft until a human approves it. AI cannot score or publish.
4. The product provides education and evidence navigation, not diagnosis, dosing, cycles, stacks, or purchase recommendations.
5. Real medical research data, marketplace provenance, secrets, and proprietary templates remain outside Git.
6. `:core` remains platform-free. Substance Atlas adds only `:features:substanceCatalog:contract` and `:features:substanceCatalog:client` as physical Gradle modules.
7. REST and MCP are inbound adapters over the same application services. PostgreSQL is the source of truth.
8. PT-BR and English are supported from the first release; source titles remain in their original language.
9. Every external source adapter has an allowlisted host, timeout, bounded response size, and deterministic fixture tests.
10. A milestone is complete only after its stated verification gate passes.

## Quality gates

- All 447 observed marketplace rows receive a parse status; silent drops are forbidden.
- Published evidence retains prior revisions and source references.
- API errors use RFC 9457 Problem Details.
- No live internet dependency exists in automated tests.
- Docker services bind only to loopback and never mount the Docker socket or broad host directories.
- Build, test, secret scan, data-boundary scan, and responsive UI checks pass before handoff.
