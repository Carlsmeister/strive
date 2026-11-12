"""Render one or more Strive 1 boards (.dc.html) locally in all four modes and screenshot them with headless Chrome.

Usage: python3 preview.py OUT.png BOARD.dc.html [BOARD2.dc.html ...] [--modes dark-hc,dark,light,light-hc]
Each board becomes a row of phones (one per mode). Only {{mode}} is substituted; <sc-if value="{{x}}"> blocks are dropped
(a board's interactive branches start closed); every other {{hole}} is left visible so a stray one is easy to spot.
"""
import os, re, subprocess, sys, tempfile, pathlib

HERE = pathlib.Path(__file__).resolve().parent
SYS = HERE.parent / "system" / "project"
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

args = sys.argv[1:]
modes = ["dark-hc", "dark", "light", "light-hc"]
if "--modes" in args:
    i = args.index("--modes"); modes = args[i + 1].split(","); del args[i:i + 2]
out, boards = args[0], args[1:]

def body_of(path):
    src = open(path, encoding="utf-8").read()
    m = re.search(r"<x-dc>(.*)</x-dc>", src, re.S)
    if not m:
        raise SystemExit(f"{path}: no <x-dc>")
    inner = re.sub(r"<helmet>.*?</helmet>", "", m.group(1), flags=re.S)
    for _ in range(4):
        inner = re.sub(r"<sc-if [^>]*>(?:(?!<sc-if ).)*?</sc-if>", "", inner, flags=re.S)
    return inner

rows = []
for b in boards:
    inner = body_of(b)
    cells = "".join(f'<div class="cell-wrap"><div class="lab">{os.path.basename(b)} · {m}</div>{inner.replace("{{mode}}", m)}</div>' for m in modes)
    rows.append(f'<div class="rowx">{cells}</div>')

html = f"""<!doctype html><html><head><meta charset="utf-8">
<link rel="stylesheet" href="{SYS}/tokens.css"><link rel="stylesheet" href="{SYS}/components/bundle.css"><link rel="stylesheet" href="{SYS.parent.parent}/canvas/project/moments.css"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Unbounded:wght@700&display=swap">
<style>body{{margin:0;background:#777;font-family:system-ui}} .rowx{{display:flex;gap:24px;padding:12px 24px}}
.lab{{font:600 12px system-ui;color:#fff;margin-bottom:6px}} .cell-wrap{{flex:none}}</style></head>
<body>{''.join(rows)}</body></html>"""
import json
for bid, name in json.load(open(HERE.parent / "assets" / "blobs.json")).items():
    html = html.replace(f"/_blob/{bid}", f"{HERE.parent}/assets/{name}")
TMP = pathlib.Path(os.environ.get("CLAUDE_JOB_DIR", HERE.parent)) / "tmp"  # 18-approved/tmp/ is gitignored
TMP.mkdir(exist_ok=True)
tmp = tempfile.NamedTemporaryFile("w", suffix=".html", delete=False, dir=TMP)
tmp.write(html); tmp.close()
w = 24 + len(modes) * (390 + 24)
h = len(boards) * (844 + 12 + 24) + 24
subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--virtual-time-budget=6000", "--hide-scrollbars", "--allow-file-access-from-files",
                f"--window-size={w},{h}", f"--screenshot={os.path.abspath(out)}", "file://" + tmp.name],
               check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
os.unlink(tmp.name)
print(out)
