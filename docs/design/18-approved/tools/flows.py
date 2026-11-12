"""The prototype's flows, tap by tap: python3 flows.py [--check | --find F-xx ... | --links BOARD-PREFIX ...]
--check  walks every path in flows.json (each flow: one or more board paths); every step must be an <a href> on the board
         before it. Prints the steps that do not play and a count.
--find   prints the shortest path for a flow through boards of its screens in the inventory's order (a starting point
         for flows.json, not a check: it cannot see whether a board shows the right content).
--links  prints each matching board's outgoing links."""
import collections, json, pathlib, re, sys
D = pathlib.Path(__file__).resolve().parent.parent
PROJ = D / "canvas" / "project"
INV = (D / "inventory.md").read_text()

screen = {"Main": "SH-02"}
for line in (D / "boards.md").read_text().splitlines():
    m = re.match(r"\| ([A-Za-z0-9-]+) \| ([A-Z]{2}-\d{2}|O-22|LT)\b", line)
    if m:
        screen.setdefault(m.group(1), "O-22" if m.group(2) == "LT" else m.group(2))
links = {f.name.removesuffix(".dc.html"): [t.removesuffix(".dc.html") for t in re.findall(r'href="([^"]+)\.dc\.html"', f.read_text())]
         for f in PROJ.glob("*.dc.html")}


def find(seq, start=None):
    seq = [s for i, s in enumerate(seq) if i == 0 or s != seq[i - 1]]
    starts = [start] if start else [b for b, s in screen.items() if s == seq[0]]
    prev, todo = {(b, 0): None for b in starts}, collections.deque((b, 0) for b in starts)
    while todo:
        b, i = todo.popleft()
        if i == len(seq) - 1:
            path, k = [], (b, i)
            while k:
                path.append(k[0]); k = prev[k]
            return path[::-1]
        for t in links.get(b, ()):
            j = i
            while j + 1 < len(seq) and seq[j + 1] == "SH-01":  # the tab bar is a tap, not a board
                j += 1
            nxt = (t, i) if screen.get(t) == seq[i] else (t, j + 1) if j + 1 < len(seq) and screen.get(t) == seq[j + 1] else None
            if nxt and nxt not in prev:
                prev[nxt] = (b, i); todo.append(nxt)
    return None


if "--links" in sys.argv:
    for p in sys.argv[sys.argv.index("--links") + 1:]:
        for b in sorted(links):
            if b == p or b.startswith(p + "-"):
                print(f"{b}: {' '.join(links[b])}")
elif "--find" in sys.argv:
    flows = {m.group(1): m.group(3) for m in re.finditer(r"^\| (F-\d+) \| ([^|]+) \| ([^|]+) \|", INV, re.M)}
    for f in sys.argv[sys.argv.index("--find") + 1:]:
        seq = re.findall(r"\b([A-Z]{2}-\d{2})\b", flows[f].split("; or")[0])
        print(f, " > ".join(find(seq) or ["NONE for " + " ".join(seq)]))
else:
    spec = json.loads((D / "flows.json").read_text())
    bad = 0
    for f, paths in spec.items():
        if f.startswith("_"):
            continue
        # A step written "~BOARD" is a device event or a gesture (lock the phone, the app killed, a scroll): not a tap.
        miss = [f"{a}→{b}" for p in paths for a, b in zip(p, p[1:]) if not b.startswith("~") and b not in links.get(a.lstrip("~"), ())]
        miss += [f"no board {b}" for p in paths for b in p if b.lstrip("~") not in links]
        if miss:
            bad += 1
            print(f"{f}: does not play: {', '.join(miss)}")
    print(f"{len([f for f in spec if not f.startswith('_')])} flows, {bad} that do not play")
