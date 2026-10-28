# 0004. Database: SQLDelight

- Status: accepted
- Date: 2026-10-28
- Issue: #33

## Context

Strive keeps Carl's training on the phone first and syncs with Supabase later (OS#17, Q4).
**This is the expensive choice in #3:** once #4 has saved sessions, changing the database
means moving Carl's data, and until export (#6) the phone and its backup hold the only copy.

Two libraries run on both iOS and Android from shared Kotlin. Neither had been checked
against the app's Kotlin 2.4.20 and Compose Multiplatform 1.12.1. SQLDelight 2.4.1 is built
against Kotlin 2.3.10; Room 2.8.5 generates its code with KSP, whose latest release (2.3.12)
says it supports Kotlin 2.4.0 but does not mention 2.4.20.

## Options

**SQLDelight.** Tables and queries are written in SQL; a Gradle plugin generates typed Kotlin
from them. Migrations are numbered `.sqm` files, and with each released schema kept in the
repository, a Gradle task checks that the migrations produce the current schema. On iOS it
uses the system's SQLite. No KSP.

**Room.** Tables are Kotlin classes with annotations; KSP generates the code. Migrations can be
generated from schema snapshots. Ships its own SQLite on iOS. KSP must keep pace with every
Kotlin upgrade, and it adds a code-generation step to a build that already swaps on 8 GB.

## Decision

**SQLDelight 2.4.1** (Carl, 2026-10-28, decision 21 in #33), on the condition that it built
with Kotlin 2.4.20 and kept the one-line-change iOS build under 60 s. It did both, so Room
was not tried.

- SQL first is close to Supabase's Postgres, which is where #8 syncs to.
- No KSP: one less tool that has to match the Kotlin version, and less work per build.
- Migrations are checked by a task in CI, not only by convention.

## Consequences

- **Easier:** a query is SQL in a `.sq` file and a typed function in Kotlin. Reads are
  `Flow`s that send again when a table changes (decision 24).
- **Harder:** every schema change after a release needs a `.sqm` migration file, and the
  released schema is kept as `src/commonMain/sqldelight/databases/<version>.db`. Forgetting
  it fails CI's migration check, not Carl's phone.
- **Where the file lives:** `strive.db` in Application Support on iOS and the app's
  database folder on Android, both inside the app's sandbox and in the phone's own backup
  (decision 25). The bundle ID is the data (ADR 0001).
- **Revisit when** SQLDelight stops building with a Kotlin upgrade the app needs, or #8's sync
  library needs Room.
