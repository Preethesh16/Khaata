# PERSON B (Deepthi) — TASK TRACKER
### Branch: `person-b-deepthi` | Scope: Android UI + Room DB + demo polish

## What happened on 2026-07-08 (read this, Preethesh)

We raced: while you were building on `person-a`, I built the **entire app in parallel** on `person-b-deepthi` (same master plan, same package `com.khaata.app`, slightly different architecture — text-pipeline Gemini via `firebase-vertexai`, `BillViewModel` + nav-compose). Both builds went green independently.

**Merge decision:** `main` keeps YOUR implementation — your agent layer is further along (real Live API `startAudioConversation()` + `FunctionDeclaration`s via `firebase-ai`, conditional google-services plugin, version catalog). My full implementation is preserved on branch `person-b-deepthi` as reference/fallback — useful bits to cherry-pick if needed:
- `strings.xml` resource file (bilingual labels)
- A simpler text-based Gemini path (`generateContent` + strict-JSON action contract) — good fallback if the Live API preview is flaky on demo day
- `viewmodel/BillViewModel.kt` with partial-transcript UI state

I verified **your `main` builds green on my machine** (64 MB APK, 2026-07-08 20:00) with the toolchain I installed at `C:\tools` (JDK 17 ARM64 + Android SDK; this laptop is Snapdragon, so builds run Windows-side: `cmd.exe → set JAVA_HOME=C:\tools\jdk17 && gradlew.bat assembleDebug`).

## Master-plan Person 2 tasks — status on `main`
- [x] App scaffold (Kotlin + Compose + mic/camera permissions)
- [x] Room DB: entities, DAOs, AppDatabase
- [x] 50-item catalog seeded (Hindi/Kannada/English, prices, stock)
- [x] Main screen: 3 zones, animated bill list, pulsing mic, SCAN/DONE
- [x] Online/offline badge (green/orange)
- [x] Low-stock red warning
- [x] Camera scan UI (CameraX + overlay + confirm dialog)
- [x] TTS Hindi confirmations
- [x] Summary screen + share-as-text
- [x] Build green on both machines
- [ ] Demo rehearsal ×3 (mine, on the day)
- [ ] Real-device install + permission check (mine)

## My demo-day checklist
- [ ] `git pull` on `main` → `gradlew.bat assembleDebug` → `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- [ ] Drop real `app/google-services.json` (from Preethesh's Firebase console) on my machine too
- [ ] Phone: Settings → Google → Voice → Offline speech recognition → download **Hindi + English (India)** (critical for airplane-mode mic)
- [ ] Phone: verify Hindi TTS voice installed (Settings → Accessibility → TTS)
- [ ] Clear app data before judging → fresh catalog, empty bill
- [ ] Rehearse 30-second script ×3 with airplane-mode toggle
- [ ] Charge phone + laptop to 100%, pack cables/power strip

## Session log (newest first)
- 2026-07-08 20:05 — Merged `person-b-deepthi` into `main` (`-s ours`: code = Person A's tree; docs kept). Removed stray `.claude/*.tmp` file from repo. Verified main builds locally. Added README.md, rewrote this tracker.
- 2026-07-08 19:30–20:00 — Built full parallel app on `person-b-deepthi` (pushed). Installed Windows-side build toolchain at `C:\tools`. First build green (69 MB APK).
