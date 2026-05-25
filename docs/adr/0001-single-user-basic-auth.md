# Single-user scope with HTTP Basic auth

FitTracker is built for one user (the author). There is no `User` entity, no per-row ownership, and no multi-tenant authorization — every record in the database belongs implicitly to the single operator. Access is gated by a single HTTP Basic credential configured via environment variables.

This is deliberate. Adding multi-user support later means: a `User` entity, FK columns on every owned entity, `WHERE user_id = ?` on every query, ownership checks on every endpoint, and a real authentication system (JWT or session). The cost is large and the value is zero while there's exactly one user. Picking single-user up front avoids carrying that complexity unused.

A future reader landing on `SecurityConfig` and seeing `httpBasic()` against a static credential should not "modernise" it to JWT/OAuth without first revisiting whether the single-user assumption still holds.
