# 0002. Project structure: one shared module, the bundle ID, Navigation 3

- Status: accepted
- Date: 2026-10-26
- Issue: #31

## Context

The foundation (#3) replaces the spike with the app every feature is built in. Three choices
made there reach every later issue: how the code is split into modules, the app's identity on
the phone, and how screens navigate. ADR 0001 fixed the wizard's structure (`androidApp`,
`shared`, `iosApp`) and versions (Kotlin 2.4.20, Compose Multiplatform 1.12.1, AGP 9.1.1,
Gradle 9.5.1), and found that **the bundle ID is where the phone keeps the app's data**.

Measured on Carl's M2 with 8 GB RAM, after the shell from #31 was in place: clean iOS build
190 s with a fresh derived-data folder, incremental 50 s after a one-line change in shared
code, and 18 s when nothing changed. #10 measured 30–70 s clean and 22 s incremental on the
wizard's app; whether its 22 s included a code change is not recorded.

## Options

**Modules.** (a) The wizard's single `shared` module, a package per feature: least Gradle
configuration on a Mac that swaps before the build starts. (b) A module per feature and a
core module: enforced boundaries and parallel compilation, at the cost of configuration time
and build logic, for an app with no features yet.

**Bundle ID.** (a) `se.carlsmeister.strive` on both platforms. (b) The wizard's pattern,
`se.carlsmeister.strive.Strive$(TEAM_ID)`, unique per developer.

**Navigation.** (a) Navigation 3, `org.jetbrains.androidx.navigation3` 1.1.2, the version
Compose Multiplatform 1.12.1's release notes pair with it, and stable. The back stack is a
list the app owns. (b) Navigation Compose, `2.10.0-beta01` for the same release: a beta, and
per-tab stacks through its save and restore options on a graph.

## Decision

**One `shared` module, bundle ID `se.carlsmeister.strive`, Navigation 3** (Carl, 2026-10-26).

- One module because nothing yet needs a boundary and the Mac pays for every module.
- The plain bundle ID because it is the same on both platforms and the team suffix adds
  nothing under our own prefix. The free personal team registered it without complaint.
- Navigation 3 because it is the stable one for our version, and #14's structure (four tabs,
  each with its own stack, and full-screen sessions above them) is a few lists rather than a
  graph.

The back stacks are plain lists rather than `rememberNavBackStack`, which skips registering
serializers for iOS. On Android, navigation resets to Home whenever the activity is
recreated: on rotation, on a change of the app's language, and when the system kills the
process. An open session closes with it. The code marks this as a shortcut.

## Consequences

- **Easier:** a feature is a package in `shared`; navigating is adding to a list.
- **Harder:** nothing stops one feature's package from reaching into another's; review does.
- **The bundle ID is fixed.** Changing it, or the team, starts the app with an empty database
  (ADR 0001).
- **Revisit modules when** an incremental iOS build after a one-line change passes about
  90 s, or two features start depending on each other's internals.
- **Revisit the plain lists when** a feature has state worth restoring after the activity is
  recreated, at the latest when #4's session is open during a rotation.
