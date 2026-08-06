# ADR 0014: External MCP before in-app chat

Status: accepted

Expose approved catalog data and draft-only research requests through Spring AI MCP Streamable HTTP. Do not ship in-app chat or a model-provider dependency in v1. This keeps the app useful without an API key and lets an external MCP client supply its own model.
