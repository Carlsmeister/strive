"""Build the "1 vs 1+2" page: one vs-<Board>.dc.html per changed board, design 1's board (inlined from v1-snap/) beside the
Strive 1+2 board (<dc-import>), each with the mode Tweak; then place them on the page `vs` in canvas.json.
Usage: python3 vs.py   (reads changes.json for the groups and diffs.json for each board's one-line caption)"""
import html, json, pathlib, re
D = pathlib.Path(__file__).resolve().parent.parent
PROJ, SNAP = D / "canvas" / "project", D / "v1-snap"
groups = json.loads((D / "changes.json").read_text())
diffs = json.loads((D / "diffs.json").read_text())
c = json.loads((PROJ / "canvas.json").read_text())
FONT = '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Unbounded:wght@700&amp;display=swap">'
TITLES = {"A": "1 vs 1+2 · Home, onboarding and empty states", "B": "1 vs 1+2 · logging, rest and the motion boards",
          "C": "1 vs 1+2 · the summary, records and the run summary", "D": "1 vs 1+2 · Progress and the comparisons"}

for k in [k for k, v in c["boards"].items() if v.get("page") == "vs"]:
    del c["boards"][k]; c["order"].remove(k)
for k in [k for k, v in c["notes"].items() if v.get("page") == "vs"]:
    del c["notes"][k]
if not any(p["id"] == "vs" for p in c["pages"]):
    c["pages"].insert(0, {"id": "vs", "name": "1 vs 1+2"})

y = 0
for g in ["A", "B", "C", "D"]:
    names = groups[g]
    c["notes"][f"t-vs-{g}"] = {"x": 0, "y": y, "text": TITLES[g], "kind": "title1", "maxW": 3712, "page": "vs"}
    y += 260; x = 0; row_h = 0
    for n in names:
        b = c["boards"][f"{n}.dc.html"]; w, h = b["w"], b["h"]
        src = (SNAP / f"{n}.dc.html").read_text()
        inner = re.search(r"<x-dc>(.*)</x-dc>", src, re.S).group(1)
        inner = re.sub(r"<helmet>.*?</helmet>", "", inner, flags=re.S).strip()
        W, H = 2 * w + 40 + 48, h + 48 + 46
        cap = html.escape(diffs.get(n, ""))
        label = html.escape(b.get("title", n).replace(" · dark-HC", ""))
        out = f"""<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>1 vs 1+2 · {label}</title>
<script src="./support.js"></script>
<link rel="stylesheet" href="ds/strive1/tokens.css">
<link rel="stylesheet" href="ds/strive1/components/bundle.css">
<link rel="stylesheet" href="moments.css">
</head>
<body>
<x-dc>
<helmet>
<style>
body{{margin:0}}
a{{color:inherit}}a:hover{{color:inherit}}
</style>
{FONT}
</helmet>
<div class="strive" data-theme="{{{{mode}}}}" style="width: {W}px; height: {H}px; box-sizing: border-box; padding: 24px; background: var(--color-surface-container-low); color: var(--color-on-surface); display: flex; gap: 40px">
<figure style="margin: 0; display: grid; gap: 10px; align-content: start; width: {w}px"><figcaption class="cap" style="font-weight: 600; height: 36px; line-height: 18px; overflow: hidden">Strive 1 · {label}</figcaption><div style="width: {w}px; height: {h}px; position: relative; overflow: hidden">
{inner}
</div></figure>
<figure style="margin: 0; display: grid; gap: 10px; align-content: start; width: {w}px"><figcaption class="cap" style="height: 36px; line-height: 18px; overflow: hidden" title="{cap}"><b>Strive 1+2</b> · {cap}</figcaption><dc-import name="{n}" mode="{{{{mode}}}}" hint-size="{w}px,{h}px"></dc-import></figure>
</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"mode":{{"editor":"enum","options":["dark-hc","dark","light","light-hc"],"default":"dark-hc"}},"$preview":{{"width":{W},"height":{H}}}}}'>
class Component extends DCLogic {{
  renderVals() {{
    return {{ mode: this.props.mode ?? 'dark-hc' }};
  }}
}}
</script>
</body>
</html>
"""
        f = f"vs-{n}.dc.html"
        (PROJ / f).write_text(out)
        if x and x + W > 3712:
            x = 0; y += row_h + 120; row_h = 0
        c["boards"][f] = {"x": x, "y": y, "w": W, "h": H, "title": f"1 vs 1+2 · {label}", "page": "vs"}
        c["order"].append(f)
        x += W + 80; row_h = max(row_h, H)
    y += row_h + 120

(PROJ / "canvas.json").write_text(json.dumps(c, ensure_ascii=False, indent=1) + "\n")
print(sum(len(groups[g]) for g in "ABCD"), "vs boards; missing captions:", [n for g in "ABCD" for n in groups[g] if n not in diffs])
