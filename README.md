# Strive

A personal training app for iPhone and Android from one Kotlin codebase: strength
sessions, GPS runs, and export of everything to Carl's own training log.

**Strength logging, early.** The app runs on Carl's iPhone: four tabs around a Start button,
text in Swedish and English (#31), themed from the design tokens and following the phone's dark
mode, contrast, transparency and text size (#30), on a local database (#33). Built so far: the
exercise library with 553 exercises (#41), and logging a workout from Start, with its summary
and History (#42). Not yet: the rest timer, templates, hints, records, plates, programmes, runs
and export. Home and Food are placeholders; Plan and Du only lead to the library and History.
CI checks every pull request (#32). The design is prototyped in `docs/design/`.
Beside those, this repository holds the plan: issue templates, the
Definition of Done and the architecture decision records.

| | |
|---|---|
| Stack | Compose Multiplatform (Kotlin), local database first, Supabase sync later |
| iPhone | Built and installed from Xcode with a free Apple ID (7-day signing) |
| CI | Linux on every pull request, required to merge: shared tests, Android build, lint (shared included), a Kover report. macOS on pull requests to `main`: iOS simulator tests and an unsigned build (#32) |
| Branches | Work happens on `dev`. `main` is for release-ready work only |
| Predecessor | `Carlsmeister/mobile-exercise-app-kotlin-strive`, Android only, archived. Used as a specification, not as code |

## Version 1

1. Spike: background GPS on iPhone with a free Apple ID
2. Design: the whole of version 1 as browser prototypes in `docs/design/` (runs in parallel with the spike)
3. Foundation: project structure, local database, design system, tests and CI
4. Strength training
5. Running
6. Export

After version 1: import, Supabase sync, nutrition. Each step is a parent issue; its
sub-issues are written when it is next, not before.

## Build and run

- **Android:** `./gradlew :androidApp:installDebug` with a device or emulator attached.
- **iPhone, over Wi-Fi:** the team ID lives in `iosApp/Configuration/Config.xcconfig`, never
  in Xcode's Signing pane (ADR 0001). `xcrun devicectl list devices` gives the phone's UDID.

  ```sh
  xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination id=<UDID> \
    -derivedDataPath build/xcode -allowProvisioningUpdates build
  xcrun devicectl device install app --device <UDID> build/xcode/Build/Products/Debug-iphoneos/Strive.app
  xcrun devicectl device process launch --device <UDID> se.carlsmeister.strive
  ```

  The free Apple ID signs for 7 days; reinstall at least weekly.
- **Tests:** `./gradlew :shared:testAndroidHostTest` (JVM), `./gradlew :shared:iosSimulatorArm64Test`
  (iOS simulator).
- **Lint:** `./gradlew lint`, both modules. **Coverage:** `./gradlew :shared:koverHtmlReport`, report
  only; `iosMain` is never in the number.
- **A new language** touches three places: a `values-<code>/strings.xml` under
  `shared/src/commonMain/composeResources`, `androidApp/src/main/res/xml/locales_config.xml`,
  and `CFBundleLocalizations` in `iosApp/iosApp/Info.plist`. A test checks only the first.
- **A schema change:** edit the `CREATE` in the `.sq` file and add `<n>.sqm` beside it, the
  migration from version n. `./gradlew :shared:verifySqlDelightMigration`, which CI runs, fails
  when they disagree with the released `databases/<n>.db`. When the new version ships, keep its
  schema with `./gradlew :shared:generateCommonMainStriveDatabaseSchema` (ADR 0004).

## How work is written down

- **Issues** follow `.github/ISSUE_TEMPLATE/`. A sub-issue states its design choice,
  acceptance criteria, test plan and how it is evaluated.
- **Definition of Done:** [`docs/definition-of-done.md`](docs/definition-of-done.md).
- **Architecture decisions:** [`docs/adr/`](docs/adr/README.md).
