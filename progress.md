# KHAATA — BUILD PROGRESS LOG

> Shared log between Person A (Preethesh, branch `person-a`) and Person B (Deepthi, branch `person-b`).
> **Rule: every prompt given to AI, every manual change, every merge — gets a dated entry here.**
> Read this top-to-bottom before you start working so you know exactly what's done.

---

## Branch protocol

- Person A works on `person-a`, Person B works on `person-b`.
- Before merging to `main`: `git fetch origin`, check the other branch, merge/rebase, resolve, push.
- Nobody force-pushes `main`. Ever.
- Commits use your own GitHub identity (SSH). No AI co-author trailers.

---

## Log (newest at bottom)

### 2026-07-09 00:50 — Person A (Preethesh)
- Created branch `person-a`.
- Created `progress.md` (this file) and `person1.md` (Person A task tracker).
- Prompt used: "complete all the phases in one shot ... check implementation md file and start building" — building the full app per `KHAATA_MASTER_PLAN.md`.
- Plan: scaffold Gradle/Kotlin/Compose project → Room DB + catalog → agent layer (Gemini Live via Firebase AI Logic, Gemma offline via MediaPipe LLM Inference, Omni Flash vision) → UI (mic, animated bill, camera, summary, TTS) → build APK → merge to main.
- Checked `origin/person-b`: **does not exist yet**. Person B has not pushed. Will keep re-checking before each merge; if still absent when Person A finishes, Person A covers Person B's scope too (UI + Room DB) so the app is complete in one shot.

### 2026-07-09 01:00 — Person A (Preethesh)
- Commit `a0cf780` — **Phase 1 done**: Gradle KTS scaffold (AGP 8.10.1, Kotlin 2.1.20, Compose BOM 2025.05, KSP), AndroidManifest with mic/camera/internet permissions, Room data layer (`Item`, `BillLine`, DAOs, `AppDatabase`), `CatalogSeeder` with all 50 kirana items (Hindi + Kannada + English names), `CatalogRepository` fuzzy matching (exact → contains → 60-entry alias map → Levenshtein).
- Note: `google-services` plugin is applied **conditionally** — project builds without `app/google-services.json`, but online mode needs the real file (see `app/google-services.json.example` for instructions).

### 2026-07-09 01:05 — Person A (Preethesh)
- Commit `49208fe` — **Phase 2 done (agent core)**:
  - `LiveApiManager.kt` — Gemini Live API (`gemini-live-2.5-flash-preview`) via **Firebase AI Logic** SDK, client-side only, no backend. Full-duplex audio via `startAudioConversation()`, all 7 tools declared as `FunctionDeclaration`s, system prompt with Hindi/Kannada number rules.
  - `OfflineModelManager.kt` — **Gemma on-device** via MediaPipe LLM Inference (LiteRT). Loads `.task` weights from `/data/local/tmp/llm/gemma.task` or app files. **Deterministic rule-based fallback parser** if weights missing — offline demo can never die.
  - `SpeechInputManager.kt` — on-device STT (`EXTRA_PREFER_OFFLINE`) for airplane-mode voice input.
  - `OmniFlashManager.kt` — camera frame → `gemini-2.5-flash` vision → product JSON → same tool pipeline. Confidence gates 0.7/0.4 per plan.
  - `ConnectivityObserver.kt` — NetworkCallback → StateFlow; `KhaataAgent` auto-picks GEMINI_LIVE vs GEMMA_OFFLINE. Live-API failure also degrades to offline.
  - `AgentTools.kt` — all 7 tools (lookup_price, add_to_bill, update_stock, check_stock, get_summary, remove_last_item, clear_bill) shared by BOTH modes. `QuantityParser.kt` — do/teen/adha/paav/dedh/ondu/eradu….
  - `TtsManager.kt` — Hindi TTS confirmations for offline + scan flows.

### 2026-07-09 01:09 — Person A (Preethesh)
- Commit (Phase 3) — **UI done** (Person B scope, covered because `origin/person-b` still absent after re-check):
  - `MainScreen.kt` — 3-zone layout per spec: ONLINE/OFFLINE badge (green/orange), animated slide-in bill list (24sp), low-stock red warning strip, TOTAL row, 96dp pulsing saffron mic, SCAN + DONE buttons, "Bolo apna order".
  - `CameraScreen.kt` — CameraX viewfinder, saffron scan overlay, capture → OmniFlash → auto-add / confirm dialog ("Yeh X hai?").
  - `SummaryScreen.kt` — itemized bill, total, SHARE (WhatsApp-ready text intent), NAYI BILL.
  - `KhaataViewModel.kt` + `MainActivity.kt` + saffron dark theme (#FF6B00, min 18sp).
- **`./gradlew assembleDebug` → BUILD SUCCESSFUL.** APK at `app/build/outputs/apk/debug/app-debug.apk` (~62 MB, includes LiteRT LLM engine native libs).
- Re-checked `origin/person-b`: still absent. Merging `person-a` → `main` and pushing. **Deepthi: branch `person-b` off latest `main`; everything above is done — pick up demo rehearsal, real-device testing, and google-services.json on your machine (see README-TESTING section at bottom).**

---

## HOW TO TEST & CONNECT THE GOOGLE STACK (read me, Deepthi)

1. **Firebase (online mode / Gemini Live + Omni Flash)**
   - console.firebase.google.com → project `Khaata` → add Android app, package `com.khaata.app` → enable **Firebase AI Logic** (Gemini Developer API, free tier) → download `google-services.json` → drop at `app/google-services.json` on BOTH laptops. Rebuild.
2. **Gemma weights (offline mode)**
   - Download the Gemma 3n E4B `.task`/LiteRT bundle from Kaggle/HuggingFace (LiteRT community) → `adb push gemma.task /data/local/tmp/llm/gemma.task` (~2 GB, start early).
   - No weights? Offline mode still works via the rule-based parser — demo-safe.
3. **Offline STT** — on the demo phone: Settings → Google → Voice → Offline speech recognition → download **Hindi + English (India)**.
4. **Build & install** — `./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk`.
5. **Test script** — grant mic+camera → speak "do kilo cheeni aur ek Parle-G" → bill lines animate in, stock ticks down, TTS confirms → airplane mode → badge flips OFFLINE → same order again → still works → "khatam" → total → 📷 scan a Maggi packet (online) → auto-adds.

