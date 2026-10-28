# 0003. Theme: generated tokens, Haze on Compose Multiplatform 1.12.1, screens under the glass

- Status: accepted
- Date: 2026-10-27
- Issue: #30

## Context

#15 designed the tokens (`docs/design/15-tokens/tokens.json`) and mapped them to Compose:
Material 3 names, four colour schemes, frosted glass on controls and overlays, solid with
Reduce Transparency. #30 turned them into the app's theme. Three choices made there reach
every later screen: where the values in Kotlin come from, which Compose Multiplatform version
the app stays on, and how a screen sits under the glass tab bar.

Two facts set the version question. **Compose Multiplatform 1.12.1 ignores iOS's text size
setting;** 1.13.0-beta01 (2026-10-23) scales text itself, which a spike branch confirmed on
Carl's iPhone. **Haze 2.0.1, the only backdrop blur for Compose on iOS and Android, requires
exactly 1.12.1 and Kotlin 2.4.20.**

## Options

**Values.** (a) `tokens.py` writes `Tokens.kt` beside the CSS and JSON, and CI fails when the
checked-in file differs. (b) Kotlin written by hand from the JSON: no generator, two copies
of every value that drift.

**Version and text size.** (a) 1.12.1 with our own provider that scales text by iOS's
content size category, plus Haze for glass. (b) 1.13.0-beta01: text size for free, a beta
underneath everything, and no Haze until it follows.

**Glass.** (a) Haze. (b) A translucent fill without blur: 62 % opaque, so text behind the bar
muddles what is on it. (c) Our own backdrop blur, which is rewriting Haze.

**Screens and the bar.** (a) Screens draw under the tab bar, as on iOS, and each pads its own
content clear of it. (b) The scaffold pads every screen above the bar: nothing ever passes
behind the glass, so the blur shows nothing.

## Decision

**Generated tokens, 1.12.1 with the text-size provider, Haze, and screens under the bar**
(Carl, 2026-10-27, decisions 14–16 in #30; screens under the bar follows from 16 and is
put to him in #30's pull request).

- Generated because the design page and the app then cannot disagree, and CI proves it.
- 1.12.1 because a beta under the whole app costs more than one small provider, and
  because Haze needs it.
- Haze because glass without blur fails legibility, and writing a backdrop blur is not ours
  to own.
- Under the bar because glass that never has anything behind it is a grey bar.

## Consequences

- **Easier:** a design change is an edit to `tokens.py` and a rerun; nothing in Kotlin is
  typed by hand. A glass surface is `Modifier.glass(state, StriveTheme.glass)` beside a
  `hazeSource` on what passes behind it.
- **Harder:** every screen applies the scaffold's padding itself, or its last row hides under
  the bar. Upgrading Compose Multiplatform waits on Haze.
- **Android's glass is unverified.** The emulator renders in software at about 5 s a frame
  under blur, and a hardware-GPU emulator did not fit on the disk. Haze uses `RenderEffect`
  on Android 12 and later; check on a real Android phone before Android is a release target.
- **Revisit when** Haze supports Compose Multiplatform 1.13 or later: move up, delete
  `TextScale.ios.kt`'s provider (its `shortcut:` comment says so), and check the tab bar
  labels still hold at the largest text size.