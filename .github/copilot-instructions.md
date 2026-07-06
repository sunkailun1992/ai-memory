# GitHub Copilot Instructions

This repository is the `ai-memory` Java/Spring Boot memory service. It owns AI short-term and long-term memory policy, authorization, deletion, archiving, masking, MySQL authority, Redis cache/locks, and Qdrant-derived semantic indexes. Before suggesting or changing code, read `AGENTS.md` and `docs/ai-coding/README.md`.

Follow these project rules:

- Follow `docs/ai-coding/AI_DIRECTORY_STRUCTURE_GUIDE.md` before adding, moving, or deleting directories.
- Keep Java code under `src/main/java/com/kellen`; tests belong under `src/test/java/com/kellen`.
- Do not nest sibling repositories such as `utils`, `user`, `gateway`, `admin-web`, or `ai` inside this repository.
- Keep Dubbo RPC interfaces and DTOs in sibling `rpc-api`; this service only implements provider code or calls published contracts.
- Do not change existing secrets, RabbitMQ addresses, Nacos addresses, database URLs, or production configuration values. Report file paths and line numbers only.
- Memory results must be authorization-scoped, source-attributed, and revocable; Qdrant is a derived index only, while MySQL remains the authoritative memory store. Short-term memory is not stored in Qdrant.
