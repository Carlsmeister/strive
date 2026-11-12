"""Write tools/new_rows.json into boards.md once: each new row after its sibling, each redraw's `shows` cell replaced."""
import json, pathlib
D = pathlib.Path(__file__).resolve().parent.parent
REG = D / "boards.md"
spec = json.loads((D / "tools" / "new_rows.json").read_text())
lines = REG.read_text().split("\n")
files = {l.split("|")[1].strip(): i for i, l in enumerate(lines) if l.startswith("| ")}
for after, f, title, shows, links in spec["rows"]:
    assert f not in files, f"{f} already in the registry"
    i = next(k for k, l in enumerate(lines) if l.startswith(f"| {after} |"))
    lines.insert(i + 1, f"| {f} | {title} | {shows} | {links} |")
    files[f] = i + 1
for f, shows in spec["redraw"].items():
    i = next(k for k, l in enumerate(lines) if l.startswith(f"| {f} |"))
    cells = lines[i].split(" | ")
    cells[2] = shows.split(";")[0] if f.startswith("PL-06") else shows
    lines[i] = " | ".join(cells)
REG.write_text("\n".join(lines))
print(len(spec["rows"]), "rows added,", len(spec["redraw"]), "rows redrawn")
