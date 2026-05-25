# Liquibase for schema migrations

Schema changes are managed by Liquibase, not Flyway. Changelogs live under `src/main/resources/db/changelog/`.

Two reasons drive the choice. First, the project runs H2 in the default dev profile and Postgres in the `docker`/prod profile (see `application.yaml`). Liquibase's database-agnostic changelog format lets the same migration run on both dialects without maintaining two parallel sets of SQL files; Flyway would require dialect-specific SQL or per-profile migration paths. Second, the author has standardised on Liquibase across other projects — familiarity reduces the chance of migration mistakes, which matters disproportionately on a personal app where data loss is unrecoverable.

A reader expecting Flyway (the more common Spring Boot default in 2026) should not switch tools without re-establishing the H2/Postgres dual-dialect path first.
