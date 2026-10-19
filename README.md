# Strive

A personal training app for iPhone and Android from one Kotlin codebase: strength
sessions, GPS runs, and export of everything to Carl's own training log.

**Nothing is built yet.** This repository holds the plan: issue templates, the
Definition of Done and the architecture decision records. Code starts with the spike.

| | |
|---|---|
| Stack | Compose Multiplatform (Kotlin), local database first, Supabase sync later |
| iPhone | Built and installed from Xcode with a free Apple ID (7-day signing) |
| CI | Linux on every pull request; macOS (iOS build and tests) on pull requests to `main` |
| Branches | Work happens on `dev`. `main` is for release-ready work only |
| Predecessor | `Carlsmeister/mobile-exercise-app-kotlin-strive`, Android only, archived. Used as a specification, not as code |

## Version 1

1. Spike: background GPS on iPhone with a free Apple ID
2. Design: the whole of version 1 in Figma (runs in parallel with the spike)
3. Foundation: project structure, local database, design system, tests and CI
4. Strength training
5. Running
6. Export

After version 1: import, Supabase sync, nutrition. Each step is a parent issue; its
sub-issues are written when it is next, not before.

## How work is written down

- **Issues** follow `.github/ISSUE_TEMPLATE/`. A sub-issue states its design choice,
  acceptance criteria, test plan and how it is evaluated.
- **Definition of Done:** [`docs/definition-of-done.md`](docs/definition-of-done.md).
- **Architecture decisions:** [`docs/adr/`](docs/adr/README.md).
