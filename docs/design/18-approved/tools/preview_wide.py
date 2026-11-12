"""Render one wide board (.dc.html) in one mode. Usage: python3 preview_wide.py BOARD OUT.png WIDTH HEIGHT [mode]"""
import json, pathlib, re, subprocess, sys, os
HERE = pathlib.Path(__file__).resolve().parent.parent
board, out, w, h = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
mode = sys.argv[5] if len(sys.argv) > 5 else "dark-hc"
src = pathlib.Path(board).read_text()
inner = re.search(r"<x-dc>(.*)</x-dc>", src, re.S).group(1)
inner = re.sub(r"<helmet>.*?</helmet>", "", inner, flags=re.S).replace("{{mode}}", mode)
for bid, name in json.load(open(HERE / "assets" / "blobs.json")).items():
    inner = inner.replace(f"/_blob/{bid}", f"{HERE}/assets/{name}")
html = (f'<html><head><link rel="stylesheet" href="{HERE}/system/project/tokens.css"><link rel="stylesheet" '
        f'href="{HERE}/system/project/components/bundle.css"><link rel="stylesheet" href="{HERE}/canvas/project/moments.css"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Unbounded:wght@700&display=swap"><style>body{{margin:0}}</style></head><body>{inner}</body></html>')
tmp = pathlib.Path(os.environ.get("CLAUDE_JOB_DIR", HERE)) / "tmp" / ("wide-" + pathlib.Path(board).stem + ".html")  # 18-approved/tmp/ is gitignored
tmp.parent.mkdir(exist_ok=True)
tmp.write_text(html)
subprocess.run(["/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", "--headless=new", "--disable-gpu", "--virtual-time-budget=6000", "--hide-scrollbars",
                "--allow-file-access-from-files", f"--window-size={w},{h}", f"--screenshot={os.path.abspath(out)}", "file://" + str(tmp)],
               check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
print(out)
