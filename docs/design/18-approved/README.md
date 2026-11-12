# 18 · The approved design: Strive 1+2

**This is the spec for the whole build.** Carl approved Strive 1+2 on 2026-11-12: design 1's
system and screens, with design 2's colour energy in the moments where something is won. Every
later stage renders the boards it builds from this folder.

- Canvas: **Strive 1+2**, https://claude.ai/artifact/Vcf9tb4Mafp5r8yqxu92TC (459 boards). It
  opens on "1 vs 1+2"; press Play on Main, on the Shell page, to use the prototype.
- System: **Strive 1**, https://claude.ai/artifact/3cuFKpGnj48MxnEphtVvav, installed on the
  canvas at `ds/strive1/` and copied here as `system/project/`.
- **The borrowed layer lives in `canvas/project/moments.css`**, on top of the system's tokens,
  not in the system. Moving it into the tokens is a build step.

These files are a copy of the OS working folder `.superpowers/strive-designs/`, taken on
2026-11-12. The board files are byte-identical to the canvas. Notes and tools still name the
old folders: read `design-1/` and `design-12/` as this folder, since design 12 began as a copy of
design 1. `review.md`, which `design-1.md` cites, stayed in the OS.

## What is here

| Path | What it is |
|---|---|
| `design-12.md` | Every board that differs from design 1 and why, and what is not verified. Read first |
| `design-1.md` | Design 1's notes. **Its polish list is binding**: each polish to a screen already built is a code change |
| `inventory.md` | 85 screens, 64 flows, the binding decisions and the sample data (Carl's own) |
| `rulings.md` | 26 rulings on the inventory's open points, all approved by Carl on 2026-11-12 |
| `boards.md` | The registry: every board, what it must show, where each tap leads |
| `canvas/project/` | The boards (`<ID>.dc.html`), `canvas.json` (pages, frames, notes) and `moments.css` |
| `system/project/` | The Strive 1 system: `tokens.css`, `components/bundle.css`, a card per component family |
| `assets/` | The photo, maps and routes; `blobs.json` maps the canvas's `/_blob/<id>` to a file here |
| `flows.json` | The 64 flows as board paths, tap by tap |
| `motion.json` | The motion notes per screen, the source of the stickies beside each row |
| `diffs.json`, `changes.json` | One line per changed board, and the groups on the "1 vs 1+2" page |
| `v1-snap/` | Design 1's boards before 1+2 changed them; `tools/vs.py` draws them beside the new ones |
| `polish-diff.json` | Copy and structure differences between design 1 and `dev`'s strings and Kotlin (2026-11-10) |
| `extra-boards.json` | The boards of the Motion and All modes pages |
| `tools/` | Python scripts, standard library only. See below |

## Render a board

From this folder, with Google Chrome in `/Applications` and a network for the display font:

```sh
python3 tools/preview.py OUT.png canvas/project/WO-02-s12.dc.html --modes dark-hc
```

`--modes` takes any of `dark`, `dark-hc`, `light`, `light-hc`, comma-separated; it defaults to
all four, one phone per mode, 390 × 844. Pass several boards for several rows. The board's
interactive branches start closed. Temp pages go to `$CLAUDE_JOB_DIR/tmp`, or to `tmp/` here,
which git ignores. A wide board (MO-*, MD-*, vs-*) takes `tools/preview_wide.py BOARD OUT.png W H
MODE`, with W and H from its frame in `canvas.json`.

**Do not open a `.dc.html` in a browser.** It links `./support.js` and `ds/strive1/`, which only
the published canvas serves; the tools stitch the same page together from this folder.

## Find a board

An ID is `<screen>-<state>`, and the screen is the inventory's (`WO-02` is the workout in
progress). The file is `canvas/project/<ID>.dc.html`. What it must show and where its taps lead
is its row in the registry: `grep -n '^| WO-02-s12 ' boards.md`. Every board of a screen:
`ls canvas/project/WO-02-*`. Its page and title: `boards["WO-02-s12.dc.html"]` in `canvas.json`.

## Tools

- **Render:** `preview.py`, `preview_grid.py` (many boards, one mode), `preview_wide.py`, `shot.py`
  (any local HTML page).
- **Check:** `lint.py` (459 boards, 7 known findings from design 1), `flows.py --check` (all 64
  flows play), `coverage.py` (the inventory against the boards), `classes.py`, `anchors.py`, `ctx.py`.
- **Build:** `build_canvas.py`, `build_modes.py`, `build_ds_cards.py`, `vs.py`, `add_board.py`,
  `prep12.py`, `head12.py`, `stickies12.py`. They rewrite `canvas.json` or boards: run them only to
  change the spec, and only with Carl.
- **History:** the rest ran once to make a change recorded in the notes. Kept so the record says how.
