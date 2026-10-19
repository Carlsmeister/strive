# Strive

Read `README.md` for what this is. This file holds the rules for working in it.

- **English** in everything committed: code, docs, issues, commits.
- **Pull requests against `dev`.** Never merge to `main` without Carl.
- **Issues follow `.github/ISSUE_TEMPLATE/`.** A sub-issue is written only when its parent
  is next; its Design, Acceptance, Test plan and Evaluation sections are filled before
  work starts.
- **Definition of Done** lives in `docs/definition-of-done.md` only. Templates copy it as
  checkboxes; do not restate it anywhere else.
- **A decision that reaches beyond one issue gets an ADR** in `docs/adr/`.
- **Tests first** in shared code. Platform code (location, files, signing) is proven by
  running on the phone, and the issue says which parts were proved how.
- **Say plainly what is not built.** The README's status line is updated in the same pull
  request that changes it.