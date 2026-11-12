"""Turn design 1's canvas index into Strive 1+2's: title, install record, launch, the new boards' frames, the row stickies.
Run once: python3 prep12.py (it refuses to place a board twice)."""
import datetime, json, pathlib
P = pathlib.Path(__file__).resolve().parent.parent / "canvas" / "project" / "canvas.json"
c = json.loads(P.read_text())
now = datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
c["title"] = "Strive 1+2"
c["createdOnFiles"] = {"v": 1, "at": now}
c["designSystems"][0]["copiedAt"] = now
c["launch"] = {"view": "canvas", "page": "vs"}

def place(name, x, y, w, h, title, page, after):
    f = f"{name}.dc.html"
    assert f not in c["boards"], f"{f} already placed"
    c["boards"][f] = {"x": x, "y": y, "w": w, "h": h, "title": title, "page": page, "is_interactive": True}
    c["order"].insert(c["order"].index(f"{after}.dc.html") + 1, f)

place("MO-record", 0, 1180, 1280, 800, "MO · a new record · dark-HC", "motion", "MO-gestures")
place("MO-finish", 1360, 1180, 1280, 800, "MO · finishing a workout or a run · dark-HC", "motion", "MO-record")
place("MO-week", 2720, 1180, 1280, 800, "MO · a full week · dark-HC", "motion", "MO-finish")
c["notes"]["t-motion-MO"]["text"] = "Motion · the signature moments, timed · Strive 1+2: wins on the win spring"

# EL-02-cmp at the end of the EL-02 row; the row's motion sticky moves right by its width.
a = c["boards"]["EL-02-8wk.dc.html"]
row = [v for v in c["boards"].values() if v.get("page") == a["page"] and v["y"] == a["y"]]
x = max(v["x"] + v["w"] for v in row) + 80
place("EL-02-cmp", x, a["y"], 1280, 800, "EL-02 · the 25 comparisons · dark-HC", a["page"], "EL-02-fewer")
for n in c["notes"].values():
    if n.get("page") == a["page"] and n["y"] == a["y"] and n["x"] >= x:
        n["x"] += 1280 + 80

P.write_text(json.dumps(c, ensure_ascii=False, indent=1) + "\n")
print("boards", len(c["boards"]), "· sticky ids on changed rows:",
      [k for k in c["notes"] if k.startswith("m-") and any(s in k for s in ("SH-02", "WO-02", "WO-06", "WO-21", "RU-05", "EL-02", "EL-03", "EX-08", "SH-06"))])
