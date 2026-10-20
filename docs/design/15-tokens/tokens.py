"""Strive's design tokens: the one place their values live.

Run `python3 docs/design/15-tokens/tokens.py` after changing a value. It checks every
text pair's contrast and writes tokens.css, plus tokens.json for the token page's tables,
beside it; it writes nothing if a pair fails.
Names follow Material 3 so strive#3 can map them one to one onto a Compose theme.
"""
import json
from pathlib import Path

# The 48 colour roles of Material 3's ColorScheme, in kebab case, per scheme.
# Light and dark follow direction B (calm, clear, native); -hc is Increase Contrast.
SCHEMES = {
    "light": {
        "primary": "#08736c", "on-primary": "#ffffff", "primary-container": "#c6efea", "on-primary-container": "#00413d", "inverse-primary": "#5ed6cb",
        "secondary": "#4a6360", "on-secondary": "#ffffff", "secondary-container": "#cce8e4", "on-secondary-container": "#2f4a47",
        "tertiary": "#8a5100", "on-tertiary": "#ffffff", "tertiary-container": "#ffddb8", "on-tertiary-container": "#4a2800",
        "background": "#f2f2f7", "on-background": "#000000", "surface": "#ffffff", "on-surface": "#000000",
        "surface-variant": "#e5e5ea", "on-surface-variant": "#5c5c61", "surface-tint": "#08736c",
        "inverse-surface": "#1c1c1e", "inverse-on-surface": "#f2f2f7",
        "error": "#c4281c", "on-error": "#ffffff", "error-container": "#ffdad6", "on-error-container": "#690005",
        "outline": "#8a8a8f", "outline-variant": "#d1d1d6", "scrim": "#000000",
        "surface-bright": "#ffffff", "surface-dim": "#dcdce1", "surface-container-lowest": "#ffffff", "surface-container-low": "#f7f7fa",
        "surface-container": "#f2f2f7", "surface-container-high": "#e9e9ee", "surface-container-highest": "#e5e5ea",
        "primary-fixed": "#c6efea", "primary-fixed-dim": "#8adfd6", "on-primary-fixed": "#00201e", "on-primary-fixed-variant": "#00504b",
        "secondary-fixed": "#cce8e4", "secondary-fixed-dim": "#b0ccc8", "on-secondary-fixed": "#05201e", "on-secondary-fixed-variant": "#334b48",
        "tertiary-fixed": "#ffddb8", "tertiary-fixed-dim": "#ffb95f", "on-tertiary-fixed": "#2b1700", "on-tertiary-fixed-variant": "#5c3400",
    },
    "dark": {
        "primary": "#40c8bd", "on-primary": "#00201e", "primary-container": "#00504b", "on-primary-container": "#a6f2ea", "inverse-primary": "#08736c",
        "secondary": "#b0ccc8", "on-secondary": "#1b3532", "secondary-container": "#334b48", "on-secondary-container": "#cce8e4",
        "tertiary": "#ffb95f", "on-tertiary": "#472a00", "tertiary-container": "#6a3c00", "on-tertiary-container": "#ffddb8",
        "background": "#000000", "on-background": "#ffffff", "surface": "#1c1c1e", "on-surface": "#ffffff",
        "surface-variant": "#2c2c2e", "on-surface-variant": "#a5a5aa", "surface-tint": "#40c8bd",
        "inverse-surface": "#f2f2f7", "inverse-on-surface": "#1c1c1e",
        "error": "#ffb4ab", "on-error": "#690005", "error-container": "#93000a", "on-error-container": "#ffdad6",
        "outline": "#8e8e93", "outline-variant": "#38383a", "scrim": "#000000",
        "surface-bright": "#3a3a3c", "surface-dim": "#000000", "surface-container-lowest": "#000000", "surface-container-low": "#121214",
        "surface-container": "#1c1c1e", "surface-container-high": "#2c2c2e", "surface-container-highest": "#3a3a3c",
        "primary-fixed": "#c6efea", "primary-fixed-dim": "#8adfd6", "on-primary-fixed": "#00201e", "on-primary-fixed-variant": "#00504b",
        "secondary-fixed": "#cce8e4", "secondary-fixed-dim": "#b0ccc8", "on-secondary-fixed": "#05201e", "on-secondary-fixed-variant": "#334b48",
        "tertiary-fixed": "#ffddb8", "tertiary-fixed-dim": "#ffb95f", "on-tertiary-fixed": "#2b1700", "on-tertiary-fixed-variant": "#5c3400",
    },
    "light-hc": {
        "primary": "#00504b", "on-primary": "#ffffff", "primary-container": "#00504b", "on-primary-container": "#ffffff", "inverse-primary": "#8adfd6",
        "secondary": "#26403d", "on-secondary": "#ffffff", "secondary-container": "#26403d", "on-secondary-container": "#ffffff",
        "tertiary": "#5a3300", "on-tertiary": "#ffffff", "tertiary-container": "#5a3300", "on-tertiary-container": "#ffffff",
        "background": "#f2f2f7", "on-background": "#000000", "surface": "#ffffff", "on-surface": "#000000",
        "surface-variant": "#e5e5ea", "on-surface-variant": "#38383a", "surface-tint": "#00504b",
        "inverse-surface": "#000000", "inverse-on-surface": "#ffffff",
        "error": "#8c1d18", "on-error": "#ffffff", "error-container": "#8c1d18", "on-error-container": "#ffffff",
        "outline": "#545458", "outline-variant": "#8a8a8f", "scrim": "#000000",
        "surface-bright": "#ffffff", "surface-dim": "#dcdce1", "surface-container-lowest": "#ffffff", "surface-container-low": "#f7f7fa",
        "surface-container": "#f2f2f7", "surface-container-high": "#e9e9ee", "surface-container-highest": "#e5e5ea",
        "primary-fixed": "#00504b", "primary-fixed-dim": "#003d39", "on-primary-fixed": "#ffffff", "on-primary-fixed-variant": "#e6fffb",
        "secondary-fixed": "#26403d", "secondary-fixed-dim": "#17302d", "on-secondary-fixed": "#ffffff", "on-secondary-fixed-variant": "#e6fffb",
        "tertiary-fixed": "#5a3300", "tertiary-fixed-dim": "#432500", "on-tertiary-fixed": "#ffffff", "on-tertiary-fixed-variant": "#fff1e3",
    },
    "dark-hc": {
        "primary": "#8aede3", "on-primary": "#000000", "primary-container": "#8aede3", "on-primary-container": "#000000", "inverse-primary": "#00504b",
        "secondary": "#dcf2ef", "on-secondary": "#000000", "secondary-container": "#dcf2ef", "on-secondary-container": "#000000",
        "tertiary": "#ffd8a8", "on-tertiary": "#000000", "tertiary-container": "#ffd8a8", "on-tertiary-container": "#000000",
        "background": "#000000", "on-background": "#ffffff", "surface": "#1c1c1e", "on-surface": "#ffffff",
        "surface-variant": "#2c2c2e", "on-surface-variant": "#d1d1d6", "surface-tint": "#8aede3",
        "inverse-surface": "#ffffff", "inverse-on-surface": "#000000",
        "error": "#ffd1cc", "on-error": "#000000", "error-container": "#ffd1cc", "on-error-container": "#000000",
        "outline": "#aeaeb2", "outline-variant": "#7c7c80", "scrim": "#000000",
        "surface-bright": "#3a3a3c", "surface-dim": "#000000", "surface-container-lowest": "#000000", "surface-container-low": "#121214",
        "surface-container": "#1c1c1e", "surface-container-high": "#2c2c2e", "surface-container-highest": "#3a3a3c",
        "primary-fixed": "#8aede3", "primary-fixed-dim": "#6fd9cf", "on-primary-fixed": "#000000", "on-primary-fixed-variant": "#002e2b",
        "secondary-fixed": "#dcf2ef", "secondary-fixed-dim": "#c2deda", "on-secondary-fixed": "#000000", "on-secondary-fixed-variant": "#0f2523",
        "tertiary-fixed": "#ffd8a8", "tertiary-fixed-dim": "#ffc47a", "on-tertiary-fixed": "#000000", "on-tertiary-fixed-variant": "#2b1700",
    },
}

# Glass (strive's own, Material has no role for it). Thicker and with a firmer edge under Increase Contrast.
MATERIALS = {
    "light": {"regular": "rgb(255 255 255 / .62)", "thick": "rgb(255 255 255 / .82)", "edge": "rgb(255 255 255 / .7)", "shadow": "0 8px 24px rgb(0 0 0 / .12)"},
    "dark": {"regular": "rgb(28 28 30 / .58)", "thick": "rgb(28 28 30 / .8)", "edge": "rgb(255 255 255 / .1)", "shadow": "0 8px 24px rgb(0 0 0 / .4)"},
    "light-hc": {"regular": "rgb(255 255 255 / .9)", "thick": "rgb(255 255 255 / .96)", "edge": "rgb(0 0 0 / .35)", "shadow": "0 8px 24px rgb(0 0 0 / .2)"},
    "dark-hc": {"regular": "rgb(28 28 30 / .92)", "thick": "rgb(28 28 30 / .97)", "edge": "rgb(255 255 255 / .4)", "shadow": "0 8px 24px rgb(0 0 0 / .5)"},
}

# Apple's text styles under Material 3's names (Q4): weight, size and line height in points.
TYPE = {
    "display-large": (600, 56, 60), "display-medium": (600, 44, 48), "display-small": (600, 34, 41),
    "headline-large": (700, 34, 41), "headline-medium": (400, 28, 34), "headline-small": (400, 22, 28),
    "title-large": (400, 20, 25), "title-medium": (600, 17, 22), "title-small": (600, 15, 20),
    "body-large": (400, 17, 22), "body-medium": (400, 15, 20), "body-small": (400, 13, 18),
    "label-large": (600, 17, 22), "label-medium": (400, 12, 16), "label-small": (400, 11, 13),
}
SPACE = [4, 8, 12, 16, 20, 24, 32, 40, 48]
SHAPE = {"extra-small": "6px", "small": "10px", "medium": "12px", "large": "16px", "extra-large": "28px", "full": "9999px"}

# Text pairs that must stay readable: (text, background). Standard schemes need WCAG AA, 4.5:1;
# Increase Contrast aims for AAA, 7:1 (Q6).
SURFACES = ["background", "surface", "surface-dim", "surface-bright", "surface-container-lowest", "surface-container-low",
            "surface-container", "surface-container-high", "surface-container-highest", "surface-variant"]
PAIRS = [(f"on-{r}", r) for r in ["primary", "primary-container", "secondary", "secondary-container", "tertiary",
                                  "tertiary-container", "error", "error-container"]]
PAIRS += [(t, s) for t in ["on-surface", "on-surface-variant"] for s in SURFACES]
PAIRS += [("on-background", "background"), ("inverse-on-surface", "inverse-surface"), ("inverse-primary", "inverse-surface")]
PAIRS += [("primary", s) for s in ["background", "surface", "surface-container-high"]] + [("error", "surface")]
PAIRS += [(f"on-{r}-fixed{v}", f"{r}-fixed{d}") for r in ["primary", "secondary", "tertiary"] for v in ["", "-variant"] for d in ["", "-dim"]]
# Borders and icons need 3:1 against what they sit on (WCAG 1.4.11).
NON_TEXT = [("outline", "surface"), ("outline", "background")]


def luminance(hex_colour):
    channels = [int(hex_colour[i:i + 2], 16) / 255 for i in (1, 3, 5)]
    r, g, b = [c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4 for c in channels]
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast(a, b):
    high, low = sorted([luminance(a), luminance(b)], reverse=True)
    return (high + 0.05) / (low + 0.05)


def failures():
    found = []
    for name, roles in SCHEMES.items():
        text_min = 7.0 if name.endswith("-hc") else 4.5
        for pairs, minimum in [(PAIRS, text_min), (NON_TEXT, 3.0)]:
            for fg, bg in pairs:
                ratio = contrast(roles[fg], roles[bg])
                if ratio < minimum:
                    found.append(f"{name}: {fg} on {bg} is {ratio:.2f}:1, needs {minimum}:1")
    return found


def block(selector, scheme, dark):
    lines = [f"  --color-{role}: {value};" for role, value in SCHEMES[scheme].items()]
    lines += [f"  --material-{key}: {value};" for key, value in MATERIALS[scheme].items()]
    lines.append(f"  color-scheme: {'dark' if dark else 'light'};")
    return f"{selector} {{\n" + "\n".join(lines) + "\n}\n"


def css():
    shared = ['  --font-family: system-ui, -apple-system, "Roboto", sans-serif;']
    shared += [f"  --type-{role}: {w} {s}px/{h}px var(--font-family);" for role, (w, s, h) in TYPE.items()]
    shared += [f"  --space-{n}: {n}px;" for n in SPACE]
    shared += [f"  --shape-{name}: {value};" for name, value in SHAPE.items()]
    shared.append("  --material-blur: 24px;")
    indent = lambda text: "".join(f"  {line}\n" if line else "\n" for line in text.splitlines())
    return "\n".join([
        "/* Generated by tokens.py. Edit the values there and run it; do not edit this file. */",
        ":root {\n" + "\n".join(shared) + "\n}\n",
        "/* Light is the default. Each scheme is also a class, so a page can show all four side by side. */",
        block(":root, .scheme-light", "light", False),
        "@media (prefers-color-scheme: dark) {\n" + indent(block(':root:not([data-theme="light"])', "dark", True)) + "}\n",
        block(':root[data-theme="dark"], .scheme-dark', "dark", True),
        "/* Increase Contrast. */",
        "@media (prefers-contrast: more) and (prefers-color-scheme: light) {\n" + indent(block(':root:not([data-theme="dark"])', "light-hc", False)) + "}\n",
        "@media (prefers-contrast: more) and (prefers-color-scheme: dark) {\n" + indent(block(':root:not([data-theme="light"])', "dark-hc", True)) + "}\n",
        "@media (prefers-contrast: more) {\n" + indent(block(':root[data-theme="light"]', "light-hc", False))
        + indent(block(':root[data-theme="dark"]', "dark-hc", True)) + "}\n",
        block(".scheme-light-hc", "light-hc", False),
        block(".scheme-dark-hc", "dark-hc", True),
    ])


if __name__ == "__main__":
    assert all(len(roles) == 48 for roles in SCHEMES.values()), {n: len(r) for n, r in SCHEMES.items()}
    assert all(roles.keys() == SCHEMES["light"].keys() for roles in SCHEMES.values()), "every scheme needs the same roles"
    problems = failures()
    if problems:
        raise SystemExit("Contrast fails, tokens.css not written:\n" + "\n".join(problems))
    here = Path(__file__).parent
    (here / "tokens.css").write_text(css())
    pairs = [{"fg": fg, "bg": bg, "kind": kind, "ratio": {n: round(contrast(r[fg], r[bg]), 2) for n, r in SCHEMES.items()}}
             for kind, group in [("text", PAIRS), ("non-text", NON_TEXT)] for fg, bg in group]
    data = {"schemes": SCHEMES, "materials": MATERIALS, "type": TYPE, "space": SPACE, "shape": SHAPE, "pairs": pairs}
    (here / "tokens.json").write_text(json.dumps(data, indent=1) + "\n")
    print(f"{len(pairs)} pairs pass in {len(SCHEMES)} schemes; wrote tokens.css and tokens.json")