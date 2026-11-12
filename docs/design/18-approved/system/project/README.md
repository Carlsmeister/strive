Strive is Carl's own training app: strength sessions logged set by set, GPS runs, the week ahead and the progress behind.
This system is **Strive as built** — the dark-first teal tokens from strive's `docs/design/15-tokens`, the decided components from
`16-components` and `17-strength`, and D's workout screen as it runs on `dev` — polished and completed for every screen the app
needs. Calm, clear, native. Teal marks what you can tap. The numbers are the hero.

## Content fundamentals

- **English UI, sentence case.** "Start workout", "Add set (2:00)", "Discard workout". The app also ships Swedish; exercise names stay
  English in every language ("Bench Press BB", "Seated Row Cable"); Carl's own words stay as he wrote them ("Push A", "Ben A",
  "Kontrollerad nedfirning, två sekunder.").
- **Verbs on buttons, the result in the label.** "Log set 2", not "Done". A logged set's button reads "Save change". A destructive
  action names what goes: "Discard workout", "Delete Push A?".
- **Numbers read like a logbook.** `82.5 kg × 8 reps` on the focus card, `80 × 8 @3` for a set (weight × reps @RIR), `+10 × 9` for added
  weight, `0:45` for time, `20 m` for distance, `5:46 /km` for pace, `about 30 min` for an estimate, `998.4 t` for a total. A thin space
  is not needed; a plain space each side of ×. The English UI writes 82.5; a Swedish phone writes 82,5.
- **The now-label is the one uppercase line**: `NOW · BENCH PRESS BB · SET 2 OF 3`, with the set kind's word in its colour
  (`· WARM-UP`, `· DROP SET`, `· TO FAILURE`). Elsewhere, no caps, no exclamation marks, no emoji.
- **Say what is missing, plainly.** "No workouts yet", "Log a workout to see your history", "Nutrition comes later".
- **Playful only where Carl asked for it**: the total lifted carries a Swedish comparison that rotates on a tap — "Lika tungt som
  ungefär 166 afrikanska elefanter."

## Visual foundations

### Colour roles (four modes)

Themes: `dark` (first, the app is dark-first), `dark-hc` (dark with Increase Contrast — **Carl's phone; judge every screen here**),
`light`, `light-hc`. Set one with `data-theme` on `.strive` (or on `<html>`); every token below switches.

- `color-background` is the screen: pure black in dark. `color-surface` holds cards and list blocks. `color-surface-container-high`
  raises controls on a card (Edit, Finish, the strip, badges, the ⏱ button). `color-surface-container-highest` fills fields, keys and
  segmented tracks. That is the whole ladder; no tonal fills.
- `color-primary` (teal) is **only what can be tapped or is selected**: filled buttons, the exercise name, the next set's ✓ ring, a chosen
  segment, links. Plain numbers are never teal.
- `color-primary-container` is the deep teal of the ⋯ pills and the rest line after the last logged set. It stays deep teal in dark-hc.
- `color-tertiary` (amber) is the rest ring, the warm-up's W, and record stars.
- Set kinds have one colour each, from one mapping: warm-up `color-tertiary`, working `color-primary` (numbered), failure
  `accent-failure` (coral, keeps its number), drop `accent-drop` (violet, D). Supersets are `accent-superset` (blue): the A/B tags,
  the edge line and the word "Superset".
- Text: `color-on-surface` on background and surface; `color-on-surface-variant` for everything secondary (Previous, units, captions,
  column labels, grey notes). Destructive text is `color-error`; the swipe-delete slab is `color-error-container` with
  `color-on-error-container`. In dark-hc they differ: text #ffbdb6 (7:1 on every container), slab #ff6961 with black.
- The Dynamic Island is the system's and always dark: `.island` (and `.sys-dark` on a piece drawn outside it) resolves the dark
  scheme's accents in the light modes (dark-hc's in light-hc), so use `var(--color-primary)` and `var(--color-tertiary)` there.
- **Increase Contrast**: never use a tonal container (`*-container` other than `primary-container`, `secondary-container`…) as a
  surface; in dark-hc they turn near-white. Use the surface roles. Every text pair holds 4.5:1, and 7:1 in the -hc modes.

### Type

The system face (SF Pro on iPhone, Roboto on Android) with tabular figures everywhere. Apple's text styles under Material 3's names:
`headline-large` 700/34/41 for large titles, `title-medium` 600/17/22 for names and bar titles, `body-large` 400/17/22 for rows,
`body-medium` 15/20 and `body-small` 13/18 for secondary text, `label-medium` 12/16 for captions and column labels, `display-small`
600/34/41 for big numbers (the rest countdown, stat values). The workout adds four built styles: `focus-value` 700/38/44 −1 tracking,
`focus-times` (the ×), `now-label` 600/13/18 +0.3, `workout-title` 700/28/34. The live run's number is 96/100 bold.

### Space, shape, surfaces

- The screen gutter is `space-16`; cards pad `space-16`; sections sit `space-16`–`space-24` apart. Scale 4 · 8 · 12 · 16 · 20 · 24 · 32 · 40 · 48.
- Corners: `shape-small` 10 for cells, badges and the ✓; `shape-medium` 12 for fields, keys and inner buttons; `shape-large` 16 for cards,
  lists, menus and wide buttons; `shape-large-increased` 20 for the workout's three cards (focus, overview, empty workout);
  `shape-extra-large` 28 for sheets, the dock's top and alerts; `shape-full` for capsules.
- **Glass only on controls and overlays**: the tab bar, the minimised bar, the dock, the rest card, menus, sheets, map controls, and the
  title bar once content scrolls under it. `material-regular` for bars and menus, `material-thick` for the dock, rest card and sheets,
  with `material-edge` hairline and `material-shadow`. Cards and lists are solid and cast no shadow.
- Lists follow rule A: a long list is one block with hairlines; a short list you start or edit is joined tiles.

### Motion

Springs for things that move, fades for colour, nothing that loops. Every value is a token in `bundle.css`:
`--motion-press` 100 ms ease-out (shrink to `state-pressed-scale` .96 and dim to .8), `--motion-release` 350 ms spring
`cubic-bezier(.3, 1.4, .5, 1)` (a slight overshoot; **in code a spring, not the curve: Compose `spring(dampingRatio = 0.6f,
stiffness = 400f)`, SwiftUI `.spring(response: 0.31, dampingFraction: 0.6)`**: about 9 % overshoot, settled in about 330 ms),
`--motion-fade` 250 ms, `--motion-stagger` 40 ms, `--motion-tick` 1 s linear for
the rest ring. Under Reduce Motion: no movement, only fades and the digits changing.

- **Log a set**: the ✓ fills teal with a press (100 ms) and a light haptic; the row's tint slides to the next row (350 ms spring); the
  rest card rises 16 pt and fades in (350 ms); the ring starts draining (1 s ticks). A record pops a star (scale 0 → 1.2 → 1, 350 ms) and an
  amber toast for 2.4 s.
- **Rest ends**: the last 3 s the ring and the digits turn teal; at 0 a sound, a long buzz, the card settles 8 pt and fades (250 ms).
- **Exercise done**: it folds into the overview card (height spring 350 ms), the next exercise unfolds below the focus card, the card's
  label cross-fades.
- **The dock** rises from the bottom on the release spring and follows the finger on its grabber; past a third, or a flick, it closes.
- **Start**: + turns 45° to ×, the panel scales from .6 under Start, the bubbles spring out along the arc 40 ms apart.
- **Minimise**: ⌄ or a drag down shrinks the workout into the bar above the tabs (Music's player); a tap on the bar grows it back.

### States

Pressed: `state-pressed-scale` and `state-pressed-opacity`; a list row turns `color-surface-container-highest` instead. Disabled:
`state-disabled-opacity`, no response. Focused field: a `border-focus` teal ring. The scrim behind alerts: `color-scrim` at
`state-scrim-opacity`. Every target is 44 × 44 (`size-touch`); the RIR segments are the one deviation (about 29 × 44 inside the card).

## Iconography

Strive's own stroke glyphs on a 24 grid: 2 pt stroke, round caps and joins, no fills except the record star, play and pause. They
are CSS masks, so they take the text colour: `<span class="ic ic-timer"></span>`, sized `s12`…`s44` (24 default). Names: home, food,
plus, minus, plan, calendar, you, search, back, chev-r, chev-l, down, up, updown, clear, x, lift, note, pin, flame, timer, clock, swap,
link, reorder, skip, sliders, trash, pencil, check, more, info, share, download, gear, run, flag, play, pause, layers, locate, cube,
expand, heart, bolt, lock, bell, shoe, bike, watch, globe, warn, sync, cloud, chart, trend, grip, mountain, external, list, ruler, plate,
gps, gauge, route, arrow-up, arrow-down, barcode, muscle, hand, eye, archive, copy, merge, star (filled), star-o, play-f, pause-f, dot.
No emoji, no logos: devices and apps are named in text.

The ⋯ glyph on the pills is the character itself in `title-medium` bold. The plates are drawn, never iconified (see Plates).

## Building a screen

Link `tokens.css` then `components/bundle.css`. A screen is one `.strive.screen` (390 × 844) with `data-theme`:

```html
<div class="strive screen" data-theme="dark-hc">
  <div class="safe"></div>                      <!-- the 54 pt status area; no fake status bar -->
  <header class="nav">…</header>                <!-- or .wtb in a workout -->
  <main class="body">…</main>                    <!-- scrolls; 16 pt gutter, 16 pt gaps, room for the tab bar -->
  <nav class="tabbar glass">…</nav>              <!-- root screens only -->
</div>
```

- **Large title**: `<header class="lt"><h1>Plan</h1>…actions</header>` inside `.body`; a sub line `.lt-sub`.
- **Navigation bar**: `.nav` with `.l` (a `.back` "‹ Plan" in teal), `h1`, `.r` (`.navtext` Done/Cancel, `.ibtn.plain` icons).
- **Tab bar**: `.tabbar.glass` with five `.tab` (icon + label); the middle is `.tab.start` holding `.dot` with `ic-plus`; the current tab
  has `aria-current="page"`. Above it, in a `.bottomstack`, the `.minibar.glass` of a minimised workout or run.
- **Buttons**: `.btn` (wide, 50 pt, teal text on `surface-container-high`), `.btn.filled` (one per view), `.btn.plain` (on background),
  `.btn.danger`, `.btn.small` (capsule), `.btn.text`, `.btn.dis`. Icon buttons `.ibtn` (44 disc), `.ibtn.plain`, `.ibtn.sm`; the ⋯ pill
  `.pill`.
- **Lists**: `.list` > `.row` (title left, `.val` or `.chev` right); `.row.lead` with an icon tile `.lic`; `.list.tiles`; `.group` with an
  `h4` heading and `.foot`. Library rows `.lrow` with `.thumb`, `.mark` badges and `.chk`; history rows `.drow` with `.dnum`.
- **Cards and numbers**: `.card` (`.eyebrow`, `.title`, `.meta`), `.stats` / `.stats.four` / `.stats.three` with `.stat` (`.k`, `.v`,
  `.sub`), `.chart` with SVG marks `.bar`, `.bar2`, `.line`, `.pt`, `.avg`, `.tick`, `.grid`, `.legend`.
- **Controls**: `.segc` switches a view (the chosen segment on surface, as iOS; in dark modes it is outlined, since surface-bright equals the track there); `.seg` picks a value (the chosen one teal, as built);
  `.seg.compact` inside the focus card. `.field` (with `.in`, `.ph`, `.focus`), `.switch` / `.switch.on`, `.chip` / `.chip.on`.
- **Overlays**: `.scrim`; `.menu` (pull-down, glass) with `.mrow`, `.tiles` > `.tile`, `.mtitle`; `.sheet.glass` (or `.sheet.full`,
  `.sheet.opaque` for a full-height form) with `.grabber` and `.sheet-head`; `.center` > `.alert` (title, message, `.acts`); `.asheet`
  (action sheet: `.list` of centred `.row`, `.row.head`, `.row.destructive`, `.row.cancel`); `.sysalert` for the system's own dialogs;
  `.hint` (first-use hint, `.up`/`.down` arrow, `--arrow` offset); `.toast`.

### The workout (D)

- **Title bar** `.wtb`: `.l` with ⌄ (`.ibtn.plain` + `ic-down`) and ⏱ (`.ibtn` + `ic-timer`), `.mid` (empty until scrolled; then
  `.wn` name + small `.pill`, and `small` "24:13 · 2/15 sets"), `.finish` (`.finish.all` once every set is logged). `.wtb.glassed.hair`
  when content runs under it.
- **Large header** `.whead`: `.name` (28 pt bold + `.pill`), `.meta` with the date, the clock (`.clock`) and "2 of 15 sets".
- **Focus card** `.focus`: `.now`, `.fvals` (`.fv` value, `.fu` unit, `.fx` ×), `.fchange` ("up 2.5 kg"), `.fmeta` ("rest 2:00 after",
  "Previous **80 × 8 @3**"), `.seg.compact` RIR 0 · 1 · 2 · 3 · 4+, `.facts` with `.fbtn` Edit and `.fbtn.log` "Log set 2".
- **Strip** `.stripwrap` > `.strip` (`.now`, `b` values + `small` RIR): one button back to the card.
- **Exercise** `.exh` (`.top`: `.nm` teal name, `.pill`, `.fold`), `.exnote` lines (pin, **Plan**, **Previous**), `.cols` labels, then
  `.srow` per set — `.badge` (`.w` `.f` `.d`), `.sprev`, `.cell.w` + `.cell.r` (or `.cell.one`), `.ok` — with `.srow.done` and
  `.srow.next` (tinted, outlined ✓); `.pline` plates line under the next barbell set; `.restl` rest lines (`.restl.cur` after the last
  logged set); `.addset`. A superset wraps its rows in `.ssgroup` under a `.sslabel`, rows carry `.tagl` letters.
- **Overview** `.ov` > `.ovrow` (`.nm` with `.tagl`, `.dots` > `i.on|w|f|d`, `.cnt` / `.cnt.all`, `.exp`). End: `.addbig`, `.discard`.
- **Empty workout** `.qs` (`.mark` 72 pt +, `h2`, `p`, `.btn.filled`), then quick picks as `.chip.qp` with `.plus`.
- **Dock** `.dock.glass.thick`: `.grab`, `.dk-title`, `.dk-fields` of `.lbl` + `.nf` (`.stepk` − / + around `.v`; `.nf.focus`;
  `.v.fresh`), the `.barline` (drawn sleeve, text, `.barmenu` "Bar 20 ▾"), `.dk-2` (RIR `.seg` + rest field), `.btn.filled` Log.
- **Keypad** `.kp`: `.top` (`.kk.small` Hide, Plates, then `.kk.pm` − +), `.digits` of `.kk` (glass, round), `.digits.plates` for plate keys.
- **Rest card** `.restcard.glass.thick`: `.top` (`.ring` + `.k` "Rest" + `.time`), `.acts` (`.roundk` −, "15 s", `.roundk` +, `.skipk` Skip).
  The ring: `<svg viewBox="0 0 64 64"><circle class="track" cx="32" cy="32" r="23.5" stroke-width="6.4"/><circle class="prog" …
  stroke-dasharray="147.65" stroke-dashoffset="(1 − left/total) × 147.65"/></svg>`.

### Plates

The bar's sleeve drawn per side: `<svg class="sleeve" viewBox="0 0 64 40">` with `.shaft` (x 0, y 18, h 4), `.collar` (x 4, 4 × 16),
then plates innermost first, 1 pt apart, centred on y 20: 25 kg 7 × 36, 20 kg 6 × 32, 15 kg 5 × 28, 10 kg 5 × 24, 5 kg 4 × 22,
2.5 kg 3 × 16, 1.25 kg 3 × 14. Text beside it: "25 + 5 + 1.25 per side". Carl's gym has no 15 kg plates.

### Plan, library, progress, running

Week card `.wk` (`.pname`, `.wkhead`, `.wbars` > `i.past|now|shown`, `.wkdays` > `.wkday` with `b` date, `.pl` plan, `.today`, `.sel`,
`.miss`, `.drop`, `.x`), `.dlrec`; day card `.card` with `.tex`; template carousel `.tcar` > `.tcard` (`.th`, `.mus`, `.tstat`, `small`),
`.tcard.new`; preview rows `.pvrow`; programme rows `.wkrow` with `.mini7`, `.wkopen`; editor grid `.egrid`. Library `.lhead` (sticky
muscle heading), `.chips`, `.srch`, `.pickbar.glass`. Exercise detail `.illo`, `.steps`, `.hcard` + `.hset`, `.xtiles` > `.xtile`, `.rm`.
History `.hweek`, `.drow`, `.prs`. Progress `.hero`, `.funline`, `.mbars` > `.mbar` (`.track`, `.band`). Running: `.map` with stacked
`<img>` (a base map, then a route), `.mapctl.glass` > `.mapbtn`, `.you`, `.gps`, `.bignum`, `.runstats`, `.runbtn` / `.runbtn.main`,
`.grid2`, `.split` / `.split.fast` / `.split.sel`. System surfaces: `.lock`, `.lockclock`, `.notif`, `.appicon`, `.la`, `.island`
(`.wide`, `.big`).

## Do and don't

- Do give every screen its real data and every state its own board; don't use lorem ipsum or empty boxes.
- Do draw a missing illustration as the drawn muscle glyph on a `surface-container-high` tile; don't leave a grey box.
- Don't put teal on a number that cannot be tapped; don't fill a container tonally in Increase Contrast.
- Don't add a second filled button to a view, a shadow to a card, or glass to content.
