# Muscle illustration prompts

Carl generates the exercise illustrations with ChatGPT's image model (2026-10-25), in place
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
Plain black fitted shorts, barefoot, short dark hair, neutral expression. Where a
coloured muscle lies under the shorts, the shorts turn semi-transparent over it so the
red muscle shows through with its own clean outline; elsewhere they stay opaque black.
Only the named muscles are coloured: primary muscles in saturated red, secondary
muscles in light, semi-transparent red. Everything else stays greyscale.
Equipment in matte dark grey and black. No text, no labels, no arrows, no logo,
no watermark, no floor, no shadow beyond a faint contact shadow.
Landscape 3:2 frame, both figures fully in view with even margins.
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

## In ChatGPT

1. Start a new chat and send the style block and the bench press block as one message.
2. Regenerate until it passes the acceptance list.
3. In the **same chat**, send only the next exercise block, starting with "Same figure, style
   and framing as the accepted image:". The chat keeps the figure consistent.
4. Download each accepted image as PNG and name it by the exercise in English, e.g.
   `bench-press.png`.

## Pilot

| Exercise | View | Start → end | Primary | Secondary |
|---|---|---|---|---|
| Bench press | Side three-quarter from the lifter's left, camera just above bench height | Lying flat on a horizontal bench, bar touching the lower chest, forearms vertical → arms straight and vertical, bar directly over the shoulders | Pectoralis major | Anterior deltoid, triceps brachii |
| Back squat | Side, from the lifter's right, camera at hip height | Standing, bar on the upper back → thighs just below parallel, knees over the toes, torso leaning slightly forward | Quadriceps, gluteus maximus | Adductor magnus |
| Lateral raise | Front | Standing, dumbbells at the sides → arms raised to shoulder height, elbows slightly bent | Lateral deltoid | Upper trapezius |

**Learned from the first bench press (2026-10-25):** a view from the feet foreshortens the
body, so a flat bench reads as an incline and the pressed bar ends up over the face. Lying
exercises are drawn from the side, and the prompt says the bench angle in words. The side view
passed (Carl, 2026-10-25): flat bench, right muscles, the style wanted. Its one flaw is the
lockout, where the arms lean toward the feet instead of standing vertical over the
shoulders; regenerate it in the batch.

**Learned from the first back squat (2026-10-25):** red painted on top of the shorts
broke them up, and muscles facing away from the camera (glutes, erector spinae in a
front view) came out as red smudges at the edges. The style block now makes the shorts
semi-transparent over a coloured muscle, and **every listed muscle must face the
camera**: choose the view for the primary muscles, and drop a secondary muscle the view
cannot show rather than let the model guess.

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

- **The model's terms.** OpenAI's terms page could not be read here (it refuses automated
  requests). As remembered, they assign the output to the user and allow commercial use;
  **check it on openai.com/policies/terms-of-use before the batch.**
- **One image or two.** Q36 puts a video on top and a muscle map under it; an image like
  this shows both the movement and the muscles, and could replace the map, or the video
  for exercises without one.