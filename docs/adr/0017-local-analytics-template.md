# ADR 0017: Keep the proprietary analytics template local

Status: accepted

The OpenAI analytics dashboard template is adapted and visually verified locally but never committed. The server reads one explicit read-only workbook path and fills it with Apache POI. The export endpoint returns a capability-unavailable problem when the template is absent; tests use a repository-safe synthetic workbook fixture.
