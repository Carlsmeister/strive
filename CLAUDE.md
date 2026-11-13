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

## Testing

The declaration the Carlsmeister OS pipeline reads (its `workflows/testing.md`). Every
command is copied from `README.md` (Build and run) or `.github/workflows/`, and plans copy
them verbatim.

| Command | Runs | Threshold |
|---|---|---|
| Full suite | `./gradlew :shared:testAndroidHostTest && ./gradlew :shared:iosSimulatorArm64Test` | A check |
| Smoke | Absent: neither the README nor CI names one; the app is proven by running it on the phone | A check |
| One test | `./gradlew :shared:testAndroidHostTest --tests '{test}'` | A check |
| Coverage on changed lines | `./gradlew :shared:koverHtmlReport` | None declared: report only, no minimum until #4 |
| Coupling change | Absent: none declared | None declared |
| Architecture rules | Absent: none declared | A check |

**The full suite takes over 40 minutes on the 8 GB Mac**, so a stage runs the tests its
change touches first.

- **The iOS flow (UI) tests** live in `shared/src/iosTest/` and run in the full suite's
  `iosSimulatorArm64Test`. No command runs them alone, and CI runs them only on pull
  requests to `main`.
- **A test is named as Gradle's `--tests` filter takes it**, `<package>.<Class>.<method>`:
  `se.carlsmeister.strive.PopTest.popRemovesTheTopScreen`. The one-test command runs the
  JVM tests only, so a test in `iosTest` is proven by the full suite.
- **Coverage is the whole `shared` module**, `commonMain` and `androidMain`, never
  `iosMain`; it does not isolate changed lines.