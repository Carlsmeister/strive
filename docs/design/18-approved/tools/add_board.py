"""Add one board to canvas.json beside an existing one, shifting that row's motion sticky: python3 add_board.py NEW AFTER "TITLE"
The index stays as published otherwise (read it from the canvas first)."""
import json, pathlib, sys
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project" / "canvas.json"
new, after, title = sys.argv[1] + ".dc.html", sys.argv[2] + ".dc.html", sys.argv[3]
c = json.loads(P.read_text())
assert new not in c["boards"], "already listed"
a = c["boards"][after]
row = [v for v in c["boards"].values() if v.get("page") == a.get("page") and v["y"] == a["y"]]
x = max(v["x"] for v in row) + 390 + 80
c["boards"][new] = {"x": x, "y": a["y"], "w": 390, "h": 844, "title": title, "page": a.get("page"), "is_interactive": True}
c["order"].insert(c["order"].index(after) + 1, new)
for n in c["notes"].values():  # the motion sticky at the row's end moves one slot right
    if n.get("page") == a.get("page") and n["y"] == a["y"] and n["x"] >= x:
        n["x"] += 470
P.write_text(json.dumps(c, ensure_ascii=False, separators=(",", ":")))
print(new, c["boards"][new])
