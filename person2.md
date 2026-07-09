# PERSON B (Deepthi) — TASK TRACKER
### Branch: `person-b` | Read `progress.md` first — the full app is already built and on `main`.

Preethesh (Person A) covered the original Person 2 build scope because `person-b` had no commits
when the one-shot build ran. **Your branch `person-b` is created and pushed — `git checkout person-b`,
work there, push, and Person A will merge.** Log everything you do in `progress.md`.

## What's already done (don't redo)
- Full Compose UI (main/camera/summary), Room DB + 50-item catalog, TTS, animations
- Dual-mode agent (Gemini Live online / Gemma + rule-parser offline), Omni Flash scan
- Unit tests green, `assembleDebug` green, demo-reset button (↺)

## Your remaining tasks (device + demo ownership)
- [ ] Put the real `app/google-services.json` on your machine too (ask Preethesh for Firebase project access)
- [ ] Run the app on the demo phone via ADB; verify mic + camera permissions flow
- [ ] Download offline Hindi + English (India) speech packs on the demo phone
- [ ] `adb push gemma.task /data/local/tmp/llm/gemma.task` once the ~2 GB download finishes
- [ ] Test full online loop: speak order → bill animates → TTS confirms
- [ ] Test airplane-mode loop: badge flips OFFLINE → same order still bills
- [ ] Test camera scan on 3 real products (Maggi, Parle-G, Colgate)
- [ ] Edge cases: "hatao" correction, unknown item, "khatam" total, "nayi bill"
- [ ] UI polish pass on the real phone (sunlight contrast, font sizes, animation feel)
- [ ] Own the 30-second demo script (in README) — rehearse 3× minimum
- [ ] Charge devices to 100%, pre-install APK (don't rely on live ADB during judging)
