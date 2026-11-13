"""Render an approved board or system card in dark and dark-HC at 3x, beside the simulator shot of the same name.

Usage: python3 tools/compare.py ID [--shots DIR]   (run from docs/design/18-approved; DIR defaults to /tmp/strive-board-shots)
ID is a board (canvas/project/ID.dc.html) or a system card (system/project/components/ID/preview.html).
For each mode it writes DIR/ID-<mode>.board.png and prints `shot  board`, or `no shot yet: shot  board`.
Scale 3 makes a 390 x 844 board 1170 x 2532 px, the simulator shot's size, so nothing is resized.
"""
import os, re, subprocess, sys, pathlib

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parent
SYS = ROOT / "system" / "project"
MODES = ["dark", "dark-hc"]


def card_width(html: str) -> int:
    # The card's first fixed width plus its 16 pt padding on each side, never narrower than a phone.
    m = re.search(r"(?<![-\w])width:\s*(\d+)px", html)
    return max(390, int(m.group(1)) + 32) if m else 390


def card_page(body, mode):
    return f"""<!doctype html><html data-theme="{mode}"><head><meta charset="utf-8">
<link rel="stylesheet" href="{SYS}/tokens.css"><link rel="stylesheet" href="{SYS}/components/bundle.css"><link rel="stylesheet" href="{ROOT}/canvas/project/moments.css"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Unbounded:wght@700&display=swap">
<style>body{{margin:0;background:var(--color-background)}}</style></head>
<body>{body}</body></html>"""


if __name__ == "__main__":
    assert card_width('<div style="width:720px;display:grid">') == 752
    assert card_width('<div style="width:358px;display:grid">') == 390
    assert card_width('<div style="display:grid">') == 390

    args = sys.argv[1:]
    shots = "/tmp/strive-board-shots"
    if "--shots" in args:
        i = args.index("--shots"); shots = args[i + 1]; del args[i:i + 2]
    if len(args) != 1:
        raise SystemExit(__doc__)
    ident = args[0]
    board = ROOT / "canvas" / "project" / f"{ident}.dc.html"
    card = SYS / "components" / ident / "preview.html"
    if not board.is_file() and not card.is_file():
        raise SystemExit(f"{ident}: neither a board ({board}) nor a system card ({card})")
    os.makedirs(shots, exist_ok=True)

    for mode in MODES:
        out = os.path.join(shots, f"{ident}-{mode}.board.png")
        if board.is_file():
            cmd = [sys.executable, str(HERE / "preview.py"), out, str(board), "--modes", mode, "--bare", "--scale", "3"]
            subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL)
        else:
            html = card.read_text(encoding="utf-8")
            m = re.search(r"<body>(.*)</body>", html, re.S)
            if not m:
                raise SystemExit(f"{card}: no <body>")
            tmp = pathlib.Path(os.environ.get("CLAUDE_JOB_DIR", ROOT)) / "tmp"  # 18-approved/tmp/ is gitignored
            tmp.mkdir(exist_ok=True)
            page = tmp / f"compare-{ident}-{mode}.html"
            page.write_text(card_page(m.group(1), mode), encoding="utf-8")
            cmd = [sys.executable, str(HERE / "shot.py"), str(page), out, str(card_width(m.group(1))), "844", "--scale", "3"]
            subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL)
            page.unlink()
        shot = os.path.join(shots, f"{ident}-{mode}.png")
        print(f"{shot}  {out}" if os.path.isfile(shot) else f"no shot yet: {shot}  {out}")
