// Strive's component behaviour (strive#16), shared like components.css: a symlink beside the page.

// Pressed starts on touch-down; :active alone does not fire reliably in iOS Safari.
document.addEventListener("pointerdown", e => {
  const el = e.target.closest(".press");
  if (!el || el.disabled || el.closest(".is-disabled")) return;
  el.classList.add("is-pressed");
  const release = () => { el.classList.remove("is-pressed"); ["pointerup", "pointercancel", "pointerleave"].forEach(t => el.removeEventListener(t, release)); };
  ["pointerup", "pointercancel", "pointerleave"].forEach(t => el.addEventListener(t, release));
});

// − and + step a number field by 2,5 kg or 1 rep (I3); a segment selects itself.
document.addEventListener("click", e => {
  const step = e.target.closest(".num .step");
  if (step) {
    const input = step.parentElement.querySelector("input"), kg = input.inputMode === "decimal";
    const now = parseFloat((input.value || input.placeholder).replace(",", ".")) || 0;
    const next = Math.max(0, now + (step.textContent === "+" ? 1 : -1) * (kg ? 2.5 : 1));
    input.value = next.toLocaleString("sv-SE");
  }
  const seg = e.target.closest(".segc .press");
  if (seg) seg.parentElement.querySelectorAll(".press").forEach(b => b.setAttribute("aria-pressed", b === seg));
});