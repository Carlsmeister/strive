"""Write one 'all modes' sheet per screen ID (its first state) and extra-boards.json for the Motion and All modes pages.
A sheet imports the screen's board three times (dark, light, light-hc); dark-hc is the board itself on its own page."""
import json, re, pathlib
HERE = pathlib.Path(__file__).resolve().parent.parent
REG = (HERE / "boards.md").read_text()
OUT = HERE / "canvas" / "project"

firsts = {}
for line in REG.splitlines():
    if line.startswith("| ") and not line.startswith("| file") and not line.startswith("|---"):
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        sid = cells[1].split(" · ")[0]
        firsts.setdefault(sid, (cells[0], cells[1].rsplit(" · ", 1)[0]))

TEMPLATE = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>{sid} in three modes</title>
<script src="./support.js"></script>
<link rel="stylesheet" href="ds/strive1/tokens.css">
<link rel="stylesheet" href="ds/strive1/components/bundle.css">
</head>
<body>
<x-dc>
<helmet>
<style>
body{{margin:0}}
a{{color:inherit}}a:hover{{color:inherit}}
</style>
</helmet>
<div class="strive" data-theme="dark" style="width: 1298px; height: 924px; box-sizing: border-box; padding: 24px; background: var(--color-surface-container-low); display: flex; gap: 40px">
<figure style="margin: 0; display: grid; gap: 10px; align-content: start"><figcaption class="cap" style="font-weight: 600">{label} · dark</figcaption><dc-import name="{file}" mode="dark" hint-size="390px,844px"></dc-import></figure>
<figure style="margin: 0; display: grid; gap: 10px; align-content: start"><figcaption class="cap" style="font-weight: 600">{label} · light</figcaption><dc-import name="{file}" mode="light" hint-size="390px,844px"></dc-import></figure>
<figure style="margin: 0; display: grid; gap: 10px; align-content: start"><figcaption class="cap" style="font-weight: 600">{label} · light-HC</figcaption><dc-import name="{file}" mode="light-hc" hint-size="390px,844px"></dc-import></figure>
</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"$preview":{{"width":1298,"height":924}}}}'>
class Component extends DCLogic {{
  renderVals() {{
    return {{}};
  }}
}}
</script>
</body>
</html>
"""
modes = []
for sid, (f, label) in firsts.items():
    name = f"MD-{f}"
    (OUT / f"{name}.dc.html").write_text(TEMPLATE.format(sid=sid, file=f, label=label))
    modes.append([name, f"{label} · dark · light · light-HC", 1298, 924])

extra = {
    "motion": {"MO": [["MO-log", "MO · logging a set and a record · dark-HC", 1280, 800], ["MO-rest", "MO · the rest timer ending · dark-HC", 1280, 800],
                      ["MO-start", "MO · Start bubbles · dark-HC", 1280, 800], ["MO-gestures", "MO · gestures and thresholds · dark-HC", 1280, 800]]},
    "modes": {},
}
# Group the mode sheets in rows of four, by area prefix.
for i in range(0, len(modes), 4):
    extra["modes"][f"MD{i // 4:02d}"] = modes[i:i + 4]
(HERE / "extra-boards.json").write_text(json.dumps(extra, indent=1))
print(len(modes), "mode sheets")
