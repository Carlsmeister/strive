# 0001. The iOS route: Compose Multiplatform, built in Xcode, signed with a free Apple ID

- Status: accepted
- Date: 2026-10-26
- Issue: #12

## Context

Strive targets Carl's iPhone and Android from one Kotlin codebase (OS#17, Q2), built on his
own Mac with a free Apple ID rather than the paid Apple Developer Program (Q10). Two
assumptions carried that plan and nobody had checked them: that a free Apple ID allows
background location, and that Xcode plus a Kotlin Multiplatform build is workable on an M2
with 8 GB RAM. The spike (#1) tested both on Carl's iPhone 13 Pro Max (iOS 26.6.1) with
Xcode 27.1 on macOS 26.6.2.

**What the spike measured** (details in #10 and #11):

- The JetBrains wizard project (Kotlin 2.4.20, Compose Multiplatform 1.12.1, AGP 9.1.1,
  Gradle 9.5.1) builds, signs with the free personal team and launches on the phone. Clean
  iOS build 30–70 s, incremental 22 s; memory pressure yellow but never red, with about 6 GB
  of swap already in use by other apps. The toolchain and caches took 11 GB of disk.
- Background location works with the free team. One 21-minute run with the screen locked:
  1,277 fixes, longest gap 1.19 s, 1 % battery. It needed only the `location` background
  mode in `Info.plist` and `allowsBackgroundLocationUpdates`; the CoreLocation code called
  from Kotlin/Native is about 75 lines.
- The whole chain works over Wi-Fi: build, sign and install with `xcodebuild` and
  `xcrun devicectl` in 30 s, no cable.
- Apple's capability table (checked 2026-10-26) allows the free account Background Modes,
  HealthKit and Maps (without routing directions), but not push notifications, iCloud or
  Sign in with Apple.

**What is not known:** whether Xcode renews the provisioning profile without help when it
expires (the first one expires 2026-11-02), whether SideStore refreshes on its own in the
background, and whether a run longer than 21 minutes behaves the same.

## Options

**A. Xcode with a free Apple ID, installed over Wi-Fi.** Costs a rebuild and reinstall from
the Mac at least every 7 days, with the phone on the same network; at most 3 own apps on the
phone and 10 App IDs a week. Gives Apple's own tooling only, measured end to end.

**B. SideStore** (installed with iloader, refreshed on the phone through LocalDevVPN, the
same free Apple ID). Costs a dependency on three community projects and a third-party
anisette server, all of which Apple broke on 2026-10-14; sign-in on the phone failed with
`-80045` until On-Device Anisette was turned off and the external `mac-ani.jkcoxson.com`
server chosen. It re-signs apps with one more team suffix
(`…StriveSpikeY327M7P3FT.Y327M7P3FT`), so an app installed through SideStore is a different
app from the one Xcode installs, with its own data. Gives refreshing without the Mac, if
background refresh works. Installed and refreshed by hand successfully on 2026-10-26.

**C. The Apple Developer Program, $99 a year.** Costs money every year. Gives one-year
signing, TestFlight for sharing with another person, and push notifications and iCloud.

## Decision

**A.** Compose Multiplatform in the wizard's structure (`androidApp`, `shared`, `iosApp`),
built in Xcode on Carl's Mac and signed with his free personal team; the team ID lives in
`iosApp/Configuration/Config.xcconfig`, never in Xcode's Signing pane. The app is reinstalled
over Wi-Fi at least every 7 days.

A because it rests on Apple's own tools alone and every step of it was measured. B worked on
the day, but only after a workaround for a break Apple caused twelve days earlier, and it has
not yet shown that it refreshes by itself. C pays for what nothing in version 1 needs yet.

**B stays on trial on Carl's phone.** If SideStore has refreshed StriveSpike on its own by
2026-11-02, a new record may make it the daily route and supersede this one; the SideStore
question already sits in #5's Open list.

## Consequences

- **Easier:** the foundation (#3) can be planned now, on the wizard's structure and versions.
  HealthKit, needed for Apple Health in #7, is allowed on the free account.
- **Harder:** a weekly reinstall, and nothing reaches another person's phone without C.
- **The bundle ID is the data.** Reinstalling with the same bundle ID keeps the local
  database; a new team ID, a deleted app, or switching between A and B starts with an empty
  one. Until export (#6) and sync (#8) exist, Carl's training data lives only on the phone.
- **For #5:** summing 1-second fixes overstated distance by 9.7 % against a Suunto 9;
  smoothing is needed before distance is summed (#11). Writing CoreLocation by hand was
  small, so own code over a library stands.
- **Check on 2026-11-02:** whether Xcode renews the profile when reinstalling over Wi-Fi,
  and whether SideStore refreshed StriveSpike by itself.
- **Revisit when** a needed capability is missing from the free account (push, iCloud),
  someone else should run the app (Carl's training partner, maybe), the weekly reinstall
  becomes a chore, or an Xcode or macOS update breaks Kotlin/Native.