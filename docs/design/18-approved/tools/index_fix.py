"""Add the fix round's boards to the published canvas index (canvas/project/canvas.json, equal to the live one), once:
each new board at the end of its sibling's row (add_board.py), EL-02-1yr retitled, the design system's record moved to v7."""
import datetime, json, pathlib, subprocess, sys
D = pathlib.Path(__file__).resolve().parent.parent
spec = json.loads((D / "tools" / "new_rows.json").read_text())
for after, f, title, *_ in spec["rows"]:
    subprocess.run([sys.executable, str(D / "tools" / "add_board.py"), f, after, title], check=True, capture_output=True)
P = D / "canvas" / "project" / "canvas.json"
c = json.loads(P.read_text())
c["boards"]["EL-02-1yr.dc.html"]["title"] = "EL-02 · 1 year · dark-HC"
c["designSystems"][0].update(version="1794298481-934d", copiedAt=datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))
P.write_text(json.dumps(c, ensure_ascii=False, separators=(",", ":")))
print(len(c["boards"]), "boards in the index")
