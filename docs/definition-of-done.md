# Definition of Done

The one home of this rule. Issue templates copy it as checkboxes so it gets ticked, not
remembered; when it changes, it changes here, and an old issue's checklist shows what
applied when that issue closed.

An item that does not apply is not deleted: tick it and write `n/a — <reason>` beside it.

## Sub-issue

- [ ] Every acceptance criterion checked
- [ ] Tests written before the code, and green (shared code)
- [ ] CI green
- [ ] Run on Carl's iPhone
- [ ] Run on Android
- [ ] Reviewed in a pull request against `dev`
- [ ] Docs and ADRs updated

**A green suite is not a running app.** The two "Run on" items are checked by starting
the app and looking at it, never inferred from tests.

## Parent issue

- [ ] All sub-issues closed
- [ ] The parent's acceptance criteria checked on the phone
- [ ] Close-out written: what we did, what surprised us, what a developer should know