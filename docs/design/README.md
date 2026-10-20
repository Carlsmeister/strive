# Design

Version 1 is designed here as clickable HTML and CSS prototypes before app code is written
(Carlsmeister/Carlsmeister-OS#17, Q12 as revised 2026-10-19). Each design issue gets one folder.

| Folder | Issue | Private page |
|---|---|---|
| [`14-navigation/`](14-navigation/index.html) | #14 Information architecture and navigation | https://claude.ai/artifact/4k4iruXJJDBqG6352uVkZH |
| [`15-tokens/`](15-tokens/index.html) | #15 Tokens as CSS custom properties | https://claude.ai/artifact/7A2qZDPnvdxupjX9aXuv3H |

## How a prototype is made

- **One folder per issue**, named `<issue>-<topic>/`, with an `index.html` that opens on its own.
- **Serve it, don't open it as a file.** The Playwright CLI blocks `file:` URLs:
  `python3 -m http.server --bind 127.0.0.1 --directory docs/design`.
- **Look at it as an iPhone does:** `npx --no-install playwright-cli open --browser=webkit --device="iPhone 15" <url>`,
  then `set-color-scheme dark` for the dark screenshot.
- **Carl reviews on his iPhone** through the page's private claude.ai link, listed above.
- **Every deliberate deviation from Apple's guidelines** is listed on the page that makes it. For now Carl's own
  use beats the guidelines; the list keeps a later App Store release cheap.
- **Every value comes from the tokens.** Colour, type, space, corners and glass live in
  [`15-tokens/tokens.py`](15-tokens/tokens.py). Change a value there and run
  `python3 docs/design/15-tokens/tokens.py`: it refuses to write if a text pair fails contrast,
  and otherwise writes `tokens.css`, which a prototype links. Never edit `tokens.css` by hand.
- **A page links `tokens.css` beside itself, never `../15-tokens/tokens.css`.** Outside
  `15-tokens/`, that file is a symlink: `ln -s ../15-tokens/tokens.css tokens.css`. The private
  page is served from a folder of its own (`/_f/<version>/`), so `../` leads out of the
  published files; checked on the phone for #16. Publishing sends the real file through the
  Artifact tool's `files`, as `{"tokens.css": "<path to 15-tokens/tokens.css>"}`.
