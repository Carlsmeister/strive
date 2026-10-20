# Design

Version 1 is designed here as clickable HTML and CSS prototypes before app code is written
(Carlsmeister/Carlsmeister-OS#17, Q12 as revised 2026-10-19). Each design issue gets one folder.

| Folder | Issue | Private page |
|---|---|---|
| [`14-navigation/`](14-navigation/index.html) | #14 Information architecture and navigation | https://claude.ai/artifact/4k4iruXJJDBqG6352uVkZH |

## How a prototype is made

- **One folder per issue**, named `<issue>-<topic>/`, with an `index.html` that opens on its own.
- **Serve it, don't open it as a file.** The Playwright CLI blocks `file:` URLs:
  `python3 -m http.server --bind 127.0.0.1 --directory docs/design`.
- **Look at it as an iPhone does:** `npx --no-install playwright-cli open --browser=webkit --device="iPhone 15" <url>`,
  then `set-color-scheme dark` for the dark screenshot.
- **Carl reviews on his iPhone** through the page's private claude.ai link, listed above.
- **Every deliberate deviation from Apple's guidelines** is listed on the page that makes it. For now Carl's own
  use beats the guidelines; the list keeps a later App Store release cheap.
- Until #15, prototypes are grey wireframes: structure, not looks.
