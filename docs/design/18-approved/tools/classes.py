"""List classes the boards use that neither the stylesheet nor the board's own <style> defines: python3 classes.py"""
import collections, pathlib, re
D = pathlib.Path(__file__).resolve().parent.parent
css = (D / "system/project/components/bundle.css").read_text() + (D / "system/project/tokens.css").read_text()
defined = set(re.findall(r"\.([A-Za-z_][\w-]*)", re.sub(r"/\*.*?\*/", "", css, flags=re.S)))
used = collections.defaultdict(set)
for f in sorted((D / "canvas/project").glob("*.dc.html")):
    src = f.read_text()
    local = set(re.findall(r"\.([A-Za-z_][\w-]*)", "".join(re.findall(r"<style>(.*?)</style>", src, re.S))))
    for cl in re.findall(r'class="([^"]*)"', src):
        for c in cl.split():
            if c not in defined and c not in local and "{{" not in c:
                used[c].add(f.stem)
for c, fs in sorted(used.items(), key=lambda x: -len(x[1])):
    print(f"{c}: {len(fs)} {sorted(fs)[:6]}")
