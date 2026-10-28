# 0005. Dependency injection: by hand

- Status: accepted
- Date: 2026-10-28
- Issue: #33

## Context

The database is the first thing more than one part of the app needs. Something has to create
it once and hand it to whatever reads and writes through it: today the settings, from #4 the
workouts. The app is one module (ADR 0002) with one object to hand around.

## Options

**By hand.** One class, `AppGraph`, creates the database and the repositories; each platform
builds it once when the app starts. A missing piece is a compile error. About twenty lines
today.

**Koin.** A library that wires classes by registration. Less code once there are many
classes; a missing registration fails at runtime, and it is one more dependency to keep
matched to Kotlin.

## Decision

**By hand** (Carl, 2026-10-28, decision 22 in #33). With one database and one repository,
a library would be more code than it replaces.

## Consequences

- **Easier:** reading `AppGraph` shows everything the app holds. Tests build what they need
  directly.
- **Harder:** each new repository is a line in `AppGraph`. Screens reach it through
  `LocalAppGraph`, which `App` provides; a screen previewed or tested alone has to provide it too.
- **Revisit when** `AppGraph` passes about a hundred lines, or a feature needs objects that
  live shorter than the app (one per open session, say).