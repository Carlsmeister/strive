# Architecture decision records

One file per decision that reaches beyond the issue that made it: a library, a storage
model, a project structure, a platform route. A choice that only affects one issue stays
in that issue's Design section.

Files are numbered in order: `NNNN-short-title.md`, starting at `0001`. A decision is
never edited after it is accepted; a new record supersedes it and says so.

## Index

- [0001. The iOS route](0001-ios-route.md) (#12)
- [0002. Project structure](0002-project-structure.md) (#31)
- [0003. Theme](0003-theme.md) (#30)
- [0004. Database: SQLDelight](0004-database.md) (#33)
- [0005. Dependency injection: by hand](0005-dependency-injection.md) (#33)
- [0006. The system layer](0006-system-layer.md) (#56)

## Template

```markdown
# NNNN. Title

- Status: proposed | accepted | superseded by NNNN
- Date: YYYY-MM-DD
- Issue: #N

## Context
What forces the decision, and what we know and do not know.

## Options
Each option with what it costs and what it gives.

## Decision
What we chose, and why that over the others.

## Consequences
What becomes easier, what becomes harder, and what would make us revisit it.
```