"""Render many boards side by side in one mode: python3 preview_grid.py OUT.png MODE PER_ROW BOARD... (390 × 844 boards)."""
import json, os, pathlib, re, subprocess, sys
HERE = pathlib.Path(__file__).resolve().parent.parent
out, mode, per = sys.argv[1], sys.argv[2], int(sys.argv[3])
boards = sys.argv[4:]
cells = []
for b in boards:
    src = pathlib.Path(b).read_text()
    inner = re.search(r"<x-dc>(.*)</x-dc>", src, re.S).group(1)
    inner = re.sub(r"<helmet>.*?</helmet>", "", inner, flags=re.S)
    for _ in range(4):
        inner = re.sub(r"<sc-if [^>]*>(?:(?!<sc-if ).)*?</sc-if>", "", inner, flags=re.S)
    cells.append(f'<div class="c"><div class="lab">{pathlib.Path(b).stem}</div>{inner.replace("{{mode}}", mode)}</div>')
html = (f'<html><head><link rel="stylesheet" href="{HERE}/system/project/tokens.css"><link rel="stylesheet" href="{HERE}/system/project/components/bundle.css"><link rel="stylesheet" href="{HERE}/canvas/project/moments.css"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Unbounded:wght@700&display=swap">'
        f'<style>body{{margin:0;background:#666}} .g{{display:grid;grid-template-columns:repeat({per},390px);gap:16px;padding:12px}} .lab{{font:600 13px system-ui;color:#fff;margin-bottom:4px}}</style>'
        f'</head><body><div class="g">{"".join(cells)}</div></body></html>')
for bid, name in json.load(open(HERE / "assets" / "blobs.json")).items():
    html = html.replace(f"/_blob/{bid}", f"{HERE}/assets/{name}")
tmp = pathlib.Path(os.environ.get("CLAUDE_JOB_DIR", str(HERE)), "tmp", "grid.html")  # 18-approved/tmp/ is gitignored
tmp.parent.mkdir(exist_ok=True)
tmp.write_text(html)
rows = (len(boards) + per - 1) // per
w, h = 24 + per * 406, 24 + rows * (844 + 36)
subprocess.run(["/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", "--headless=new", "--disable-gpu", "--virtual-time-budget=6000", "--hide-scrollbars",
                "--allow-file-access-from-files", f"--window-size={w},{h}", f"--screenshot={os.path.abspath(out)}", "file://" + str(tmp)],
               check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
print(out)
