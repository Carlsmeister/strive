# Architecture decision records

One file per decision that reaches beyond the issue that made it: a library, a storage
model, a project structure, a platform route. A choice that only affects one issue stays
in that issue's Design section.

Files are numbered in order: `NNNN-short-title.md`, starting at `0001`. A decision is
never edited after it is accepted; a new record supersedes it and says so.

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