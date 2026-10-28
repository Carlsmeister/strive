"""Strive's design tokens: the one place their values live.

Run `python3 docs/design/15-tokens/tokens.py` after changing a value. It checks every
text pair's contrast and writes tokens.css, plus tokens.json for the token page's tables,
beside it, and the app's Tokens.kt (strive#30); it writes nothing if a pair fails. CI runs
it and fails if any of the three changed.
Names follow Material 3 so strive#3 can map them one to one onto a Compose theme.
"""
import json
import re
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
BLUR = "24px"
SHAPE = {"extra-small": "6px", "small": "10px", "medium": "12px", "large": "16px", "extra-large": "28px", "full": "9999px"}

# Component values from strive#16, all Strive's own. Sizes: the 44-point tap area Apple asks for, icons, the rest timer's ring.
SIZE = {"touch": "44px", "icon": "24px", "ring-small": "64px"}
# Lines: a list's hairline and a focused field's ring.
BORDER = {"hairline": ".5px", "focus": "2px"}
# States (Q2): a press shrinks and dims; disabled fades; the scrim dims what is behind a sheet or a dialog.
STATE = {"pressed-scale": ".96", "pressed-opacity": ".8", "disabled-opacity": ".4", "scrim-opacity": ".32", "veil-opacity": ".08"}
# Motion (Q2): press in fast, spring back with a little overshoot, fade colours.
MOTION = {"press": "100ms ease-out", "release": "350ms cubic-bezier(.3, 1.4, .5, 1)", "fade": "250ms ease-out", "spin": "900ms linear infinite", "tick": "1s linear", "stagger": "40ms"}

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
    shared.append(f"  --material-blur: {BLUR};")
    for group, values in [("size", SIZE), ("border", BORDER), ("state", STATE), ("motion", MOTION)]:
        shared += [f"  --{group}-{name}: {value};" for name, value in values.items()]
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


KOTLIN = Path(__file__).parents[3] / "shared/src/commonMain/kotlin/se/carlsmeister/strive/theme/Tokens.kt"


def camel(name):
    head, *rest = re.split(r"-", name)
    return head + "".join(word.capitalize() for word in rest)


def argb(css_colour):
    """'#08736c' or 'rgb(255 255 255 / .62)' as Compose's 0xAARRGGBB."""
    if css_colour.startswith("#"):
        return "0xFF" + css_colour[1:].upper()
    r, g, b, a = re.fullmatch(r"rgb\((\d+) (\d+) (\d+) / ([\d.]+)\)", css_colour).groups()
    return "0x" + "".join(f"{int(v):02X}" for v in [round(float(a) * 255), r, g, b])


def px(value):
    return float(value.removesuffix("px"))


def dp(value):
    number = px(value) if isinstance(value, str) else float(value)
    return f"{number:g}.dp" if number != int(number) else f"{int(number)}.dp"


def easing(name):
    named = {"ease-out": "EaseOut", "linear": "LinearEasing"}
    if name in named:
        return named[name]
    x1, y1, x2, y2 = re.fullmatch(r"cubic-bezier\((.*)\)", name).group(1).split(", ")
    return f"CubicBezierEasing({x1}f, {y1}f, {x2}f, {y2}f)"


def motion(name, value):
    """'350ms cubic-bezier(...)' as a generic tween; 'infinite' repeats; a bare duration is a delay in ms."""
    duration, _, rest = value.partition(" ")
    millis = int(float(duration.removesuffix("ms")) if duration.endswith("ms") else float(duration.removesuffix("s")) * 1000)
    if not rest:
        return f"    const val {camel(name)}Millis = {millis}"
    spec = f"tween<T>({millis}, easing = {easing(rest.removesuffix(' infinite'))})"
    if rest.endswith(" infinite"):
        return f"    fun <T> {camel(name)}(): InfiniteRepeatableSpec<T> = infiniteRepeatable({spec})"
    return f"    fun <T> {camel(name)}(): TweenSpec<T> = {spec}"


def holder(name, doc, fields):
    """An immutable class for one of Strive's own groups, with its values as defaults."""
    lines = [f"/** {doc} */", "@Immutable", f"internal class {name}("]
    lines += [f"    val {field}: {kind} = {value}," for field, kind, value in fields]
    return "\n".join(lines + [")"])


def kotlin():
    shadows = {re.fullmatch(r"0 (\d+)px (\d+)px (rgb\(.*\))", m["shadow"]).groups()[:2] for m in MATERIALS.values()}
    assert len(shadows) == 1, "Tokens.kt assumes one shadow offset and radius for every scheme"
    (shadow_y, shadow_radius), = shadows
    out = [
        "// Generated by docs/design/15-tokens/tokens.py. Edit the values there and run it; do not edit this file.",
        "package se.carlsmeister.strive.theme",
        "",
        *[f"import {i}" for i in [
            "androidx.compose.animation.core.CubicBezierEasing", "androidx.compose.animation.core.EaseOut",
            "androidx.compose.animation.core.InfiniteRepeatableSpec", "androidx.compose.animation.core.LinearEasing",
            "androidx.compose.animation.core.TweenSpec", "androidx.compose.animation.core.infiniteRepeatable",
            "androidx.compose.animation.core.tween", "androidx.compose.foundation.shape.RoundedCornerShape",
            "androidx.compose.material3.ColorScheme", "androidx.compose.material3.Shapes", "androidx.compose.material3.Typography",
            "androidx.compose.material3.darkColorScheme", "androidx.compose.material3.lightColorScheme",
            "androidx.compose.runtime.Immutable", "androidx.compose.ui.graphics.Color", "androidx.compose.ui.text.TextStyle",
            "androidx.compose.ui.text.font.FontWeight", "androidx.compose.ui.unit.Dp", "androidx.compose.ui.unit.dp",
            "androidx.compose.ui.unit.sp"]],
        "",
    ]
    for scheme, roles in SCHEMES.items():
        builder = "darkColorScheme" if scheme.startswith("dark") else "lightColorScheme"
        out.append(f"internal val {camel(scheme)}Colors: ColorScheme = {builder}(")
        out += [f"    {camel(role)} = Color({argb(value)})," for role, value in roles.items()]
        out.append(")\n")
    out.append(holder("StriveMaterials", "Glass: a translucent fill, its edge and its shadow colour.",
                      [(f, "Color", "Color.Unspecified") for f in ["regular", "thick", "edge", "shadow"]]) + "\n")
    for scheme, m in MATERIALS.items():
        shadow = re.search(r"rgb\(.*\)", m["shadow"]).group(0)
        values = ", ".join(f"{k} = Color({argb(v)})" for k, v in [("regular", m["regular"]), ("thick", m["thick"]),
                                                                  ("edge", m["edge"]), ("shadow", shadow)])
        out.append(f"internal val {camel(scheme)}Materials = StriveMaterials({values})")
    out += ["", f"internal val MaterialBlur: Dp = {dp(BLUR)}",
            f"internal val MaterialShadowOffsetY: Dp = {dp(shadow_y)}", f"internal val MaterialShadowRadius: Dp = {dp(shadow_radius)}", ""]
    out.append("// Numbers use tabular figures (strive#15, Q5); tnum changes digits only.")
    out.append("internal val StriveTypography = Typography(")
    out += [f'    {camel(r)} = TextStyle(fontWeight = FontWeight({w}), fontSize = {s}.sp, lineHeight = {h}.sp, fontFeatureSettings = "tnum"),'
            for r, (w, s, h) in TYPE.items()]
    out.append(")\n")
    shapes = {k: v for k, v in SHAPE.items() if k != "full"}
    out.append("internal val StriveShapes = Shapes(")
    out += [f"    {camel(k)} = RoundedCornerShape({dp(v)})," for k, v in shapes.items()]
    out += [")", "", "/** `full`: a pill, whatever the height. */", "internal val ShapeFull = RoundedCornerShape(percent = 50)", ""]
    out.append(holder("Spacing", "The spacing scale in dp, `s16` for 16.", [(f"s{n}", "Dp", dp(n)) for n in SPACE]) + "\n")
    out.append(holder("Sizes", "Tap area, icon and the rest timer's ring.", [(camel(k), "Dp", dp(v)) for k, v in SIZE.items()]) + "\n")
    out.append(holder("Borders", "A list's hairline and a focused field's ring.", [(camel(k), "Dp", dp(v)) for k, v in BORDER.items()]) + "\n")
    out.append(holder("States", "Press, disabled, scrim and veil as fractions.", [(camel(k), "Float", f"{float(v):g}f") for k, v in STATE.items()]) + "\n")
    out.append("/** Durations and curves from the token file; whether to animate at all is the component's call, see reduceMotion. */")
    out.append("internal object Motion {")
    out += [motion(k, v) for k, v in MOTION.items()]
    out.append("}")
    return "\n".join(out) + "\n"


if __name__ == "__main__":
    assert all(len(roles) == 48 for roles in SCHEMES.values()), {n: len(r) for n, r in SCHEMES.items()}
    assert all(roles.keys() == SCHEMES["light"].keys() for roles in SCHEMES.values()), "every scheme needs the same roles"
    assert argb("rgb(255 255 255 / .62)") == "0x9EFFFFFF" and argb("#08736c") == "0xFF08736C"
    assert easing("cubic-bezier(.3, 1.4, .5, 1)") == "CubicBezierEasing(.3f, 1.4f, .5f, 1f)"
    problems = failures()
    if problems:
        raise SystemExit("Contrast fails, tokens.css not written:\n" + "\n".join(problems))
    here = Path(__file__).parent
    (here / "tokens.css").write_text(css())
    pairs = [{"fg": fg, "bg": bg, "kind": kind, "ratio": {n: round(contrast(r[fg], r[bg]), 2) for n, r in SCHEMES.items()}}
             for kind, group in [("text", PAIRS), ("non-text", NON_TEXT)] for fg, bg in group]
    data = {"schemes": SCHEMES, "materials": MATERIALS, "type": TYPE, "space": SPACE, "shape": SHAPE,
            "size": SIZE, "border": BORDER, "state": STATE, "motion": MOTION, "pairs": pairs}
    (here / "tokens.json").write_text(json.dumps(data, indent=1) + "\n")
    KOTLIN.parent.mkdir(parents=True, exist_ok=True)
    KOTLIN.write_text(kotlin())
    print(f"{len(pairs)} pairs pass in {len(SCHEMES)} schemes; wrote tokens.css, tokens.json and Tokens.kt")