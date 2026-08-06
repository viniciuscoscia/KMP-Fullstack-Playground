# ADR 0016: Private runtime data and public implementation

Status: accepted

The public repository contains code, migrations, contracts, and synthetic fixtures only. Real marketplace rows, medical research snapshots, review decisions, secrets, database volumes, and generated reports live under ignored local paths or Docker volumes. Automated scans enforce the boundary.
