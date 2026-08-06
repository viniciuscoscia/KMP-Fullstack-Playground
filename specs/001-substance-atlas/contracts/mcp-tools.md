# MCP Contract

Transport: Spring AI MCP Server over Streamable HTTP at `/mcp`. Every request requires `Authorization: Bearer <LOCAL_ADMIN_TOKEN>`.

## Tools

| Tool | Input | Result | Side effect |
|---|---|---|---|
| `search_substances` | query, locale, tags, page, size | paged approved summaries | none |
| `get_substance` | substanceId, locale | approved detail | none |
| `compare_substances` | 2-5 ids, indicationId, locale | aligned assessments with unknowns preserved | none |
| `list_evidence` | substanceId, indicationId?, page, size | claims and source metadata | none |
| `get_research_status` | researchJobId | durable job/item state | none |
| `request_substance_research` | 1-100 terms, locale, idempotencyKey | draft research job | creates draft only |

## Resources

- `substance://{id}` returns an approved localized representation.
- `research-job://{id}` returns job state without raw source payloads.

Tools never expose secrets, raw licensed payloads, unpublished drafts through read operations, dosage guidance, cycles, stacks, or purchasing advice.
