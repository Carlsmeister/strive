# Muscle illustration prompts

Carl can generate the exercise illustrations with an image model (2026-10-25), in place
of a licensed set (strive#4) or a map Strive draws itself (#17 Q36). This is the prompt
spec. **Pilot three exercises before any batch**, and check every image against the
acceptance list: a model that draws the wrong muscle teaches the wrong exercise.

## How a prompt is built

Every prompt is the **style block**, unchanged, followed by one **exercise block**.
Never name an illustrator, brand or app in a prompt, and never give the model a
commercial image as a reference: describe the look in words.

### Style block (the same for every exercise)

```
Anatomical fitness illustration on a pure white background. One athletic adult male
figure shown twice side by side: left the start position, right the end position of
the movement. Realistic proportions, smooth greyscale 3D rendering with soft studio
lighting, every superficial muscle visible as defined anatomy, skin in light grey.
Plain black fitted shorts, barefoot, short dark hair, neutral expression.
Only the named muscles are coloured: primary muscles in saturated red, secondary
muscles in light, semi-transparent red. Everything else stays greyscale.
Equipment in matte dark grey and black. No text, no labels, no arrows, no logo,
no watermark, no floor, no shadow beyond a faint contact shadow.
Wide 16:9 frame, both figures fully in view with even margins.
```

### Exercise block (one per exercise)

```
Exercise: <name in English>.
Equipment: <e.g. flat bench and Olympic barbell with two black plates>.
View: <front three-quarter | side | back three-quarter>, chosen so the primary
muscles face the viewer.
Start position: <one sentence>.
End position: <one sentence>.
Primary muscles (saturated red): <list>.
Secondary muscles (light red): <list>.
```

## Pilot

| Exercise | View | Start → end | Primary | Secondary |
|---|---|---|---|---|
| Bench press | Front three-quarter, from slightly above | Lying on a flat bench, bar touching the lower chest → arms straight, bar over the shoulders | Pectoralis major | Anterior deltoid, triceps brachii |
| Back squat | Front three-quarter | Standing, bar on the upper back → thighs below parallel, knees over the toes | Quadriceps, gluteus maximus | Adductor magnus, erector spinae |
| Lateral raise | Front | Standing, dumbbells at the sides → arms raised to shoulder height, elbows slightly bent | Lateral deltoid | Upper trapezius |

## Acceptance

Reject and regenerate an image when any of these fails:

1. Only the listed muscles are red, and they are on the correct place in the body.
2. The muscle shown red is visible from the chosen view.
3. Both positions show the exercise correctly: grip, bar path, joint angles.
4. Hands have five fingers; the bar is straight and level; plates are round.
5. The figure, rendering and margins match the pilot images, so the library looks like
   one set.
6. No text or marks anywhere in the image.

## Open before a batch

- **The model's terms.** Which model, and whether its output may be stored in the app
  and, later, shipped on the App Store.
- **One image or two.** Q36 puts a video on top and a muscle map under it; an image like
  this shows both the movement and the muscles, and could replace the map, or the video
  for exercises without one.