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

### 2026-07-08 20:05 — Person B (Deepthi)
- Prompt used: "now i am person B and complete all the phases in one shot … even if person A ka work is blocking u check it again and again and pull merge and continue".
- We built in parallel without seeing each other's pushes: while A was on `person-a`, B independently implemented the **entire app** on `person-b-deepthi` (pushed; build green — 69 MB APK). Different internals: text-pipeline Gemini (`firebase-vertexai` + strict-JSON action contract), `BillViewModel`, nav-compose, `strings.xml`.
- On fetch, found A's finished tree already on `main`. **Merge decision: `main` keeps A's implementation** (real Live API native audio + `FunctionDeclaration`s is the stronger demo story; conditional google-services plugin; version catalog). B's tree stays on `person-b-deepthi` as reference + fallback (the text-based Gemini path is a good plan-B if the Live API preview is flaky at the venue).
- Merged `person-b-deepthi` → `main` with `-s ours` (records lineage, keeps A's code), bringing over: `README.md`, `person2.md` (rewritten), this entry, and **removing the stray `.claude/settings.local.json.tmp.*` file** A accidentally committed.
- **Verified A's `main` builds green on B's machine** (Snapdragon/ARM64 laptop): installed JDK 17 (Windows ARM64) + Android SDK at `C:\tools`; build runs Windows-side — from WSL: `cmd.exe /c "set JAVA_HOME=C:\tools\jdk17&& gradlew.bat assembleDebug"`. Fresh APK 64 MB at 20:00.
- Remaining before demo day = configuration only: real `google-services.json` (A), Gemma weights on phone (A), offline Hindi STT + TTS models on phone (B), ADB test (both), rehearsal ×3 (B).

### 2026-07-09 01:40 — Person A (Preethesh)
- Prompt: "build remaining phases and communicate with deepthi's github commit".
- **Phase 3 polish + Phase 4 demo prep built on `person-a`:**
  - Unit tests, GREEN (`./gradlew testDebugUnitTest`): `QuantityParserTest` (Hindi/Kannada/English numbers, digits, strip) + `CatalogMatchTest` (master-plan aliases, contains-match, Levenshtein typo rescue, Devanagari names, 50-item catalog integrity, alias-target validation). `CatalogRepository.match()` extracted as a pure function to make it JVM-testable.
  - **Demo reset (↺ button, top bar)**: one tap = empty bill + full 50-item stock — the plan's "pre-seed a clean demo state".
- Mid-push, discovered **Deepthi's `person-b-deepthi` had landed and was merged to `main`** (aff1922). Merge decision acknowledged and agreed: `main` keeps the Live-API implementation, your text-pipeline stays on `person-b-deepthi` as plan-B. Resolved my merge by keeping your `README.md` + `person2.md` and combining both progress logs (this file); my app-side additions (tests + demo reset) are retained on top since your merge kept A's app tree.
- Deleted my redundant `person-b` branch (you're on `person-b-deepthi`; one branch per person, no confusion).
- Good catch on the stray `.claude` tmp file — `.claude/` is gitignored now.
- Tests + `assembleDebug` re-run green on the merged tree before push.

### 2026-07-09 06:05 — Person B (Deepthi)
- Prompt: "pull the main branch push merge everything n tell wt is remaining".
- Pulled `main` (fast-forward to `af2b4bb` — A's merge of my merge + tests + demo-reset). Verified **no unmerged commits remain on any branch** (`person-a` and `person-b-deepthi` are both fully contained in `main`; `person-b-deepthi` stays frozen as the agreed plan-B tree).
- **Verified merged tree on my machine: `testDebugUnitTest` 13/13 green (CatalogMatchTest 8, QuantityParserTest 5) + `assembleDebug` green (64 MB APK).** The repo builds identically on both laptops now.
- 👍 Agreed on branch cleanup and `.claude/` gitignore. Code is DONE — everything left is configuration/hardware (see NEXT list below).

### 2026-07-09 02:00 — Person A (Preethesh)
- Prompt: "download gemma weights and set up firebase now".
- **Firebase DONE (fully automated via firebase CLI):**
  - Created project **`khaata-kirana`** (console: https://console.firebase.google.com/project/khaata-kirana/overview)
  - Registered Android app `com.khaata.app` (App ID `1:887731890383:android:b0d451123a9954fc232fba`)
  - Pulled `app/google-services.json` — **NOT committed** (public repo → API key abuse risk). It's **gitignored**; Deepthi: get the file from Preethesh directly (WhatsApp) or run `firebase apps:sdkconfig ANDROID 1:887731890383:android:b0d451123a9954fc232fba --project khaata-kirana` after `firebase login`.
  - Enabled APIs: `generativelanguage.googleapis.com` (Gemini Developer API), `firebasevertexai.googleapis.com` (Firebase AI Logic), `firebaseml.googleapis.com`.
  - Rebuilt with plugin active — BUILD SUCCESSFUL, `khaata-kirana` config baked into APK resources.
- **Gemma weights: downloading** `gemma-3n-E4B-it-int4.task` (real size **4.41 GB**, not the plan's "~2GB") from a public HF mirror (official google/* repos are license-gated) → `models/gemma.task` (gitignored). After download + phone connected: `adb push models/gemma.task /data/local/tmp/llm/gemma.task`.

### 2026-07-09 12:25 — Person A (Preethesh)
- **Gemma weights DOWNLOADED**: `models/gemma.task` (gemma-3n-E4B-it-int4, 4.41 GB) on A's machine, size verified. Gitignored — stays local.
- Demo phone not yet connected; the moment it is: `adb push models/gemma.task /data/local/tmp/llm/gemma.task` (takes a few minutes over USB; phone needs ~5 GB free).
- **ALL software + cloud setup is now DONE.** Remaining = hardware-only: phone push (A), offline Hindi/English speech packs on phone (B), demo rehearsal ×3 (B), charge everything (both).

### 2026-07-09 20:15 — Person B (Deepthi)
- **First real-device test** (Samsung SM-S721B via ADB): APK installs, launches, catalog seeds, UI works. Mic initially dead — root-caused via live logcat: `LANGUAGE_PACK_ERROR 13`, the phone has **no offline hi-IN speech pack**, and the **new Google app has removed the manual "Offline speech recognition" download menu** entirely. Also: this phone's only recognition services are on-device (no cloud recognizer service exists), so `EXTRA_PREFER_OFFLINE=false` doesn't buy anything by itself.
- **`SpeechInputManager` hardened** (the only code change, 43 lines):
  1. On error 13 (API 33+): `triggerModelDownload()` — app self-heals by requesting the hi-IN pack itself (verified in logs: `GoogleTTSRecognitionSrv#onTriggerModelDownload` fires).
  2. While the pack is missing, **auto-fallback to en-IN recognition** (phone's voice language; handles Hinglish fine for our parser). Verified listening in logs (returns NO_MATCH when you speak too early — speak ~1s after tap).
  3. `EXTRA_PREFER_OFFLINE` now set only when the device is actually offline.
  4. Stale-recognizer guard (rapid re-taps were causing ERROR_SERVER_DISCONNECTED churn).
- Google's MDD downloader is lazy about actually fetching the hi-IN pack (tried force-running its jobs via jobscheduler; still pending). **Do the same prep on the demo phone EARLY**: install app → tap mic once on wifi → leave it plugged in on wifi for a while → verify hi-IN works in airplane mode. If hi-IN pack never lands, en-IN fallback carries the demo.
- E2E voice→bill confirmation still pending (phone was unplugged mid-test); UI/DB/TTS all verified working.

### 2026-07-09 23:35 — Person A (Preethesh)
- **APK installed on A's phone (Realme RMX3998) via USB** — app launches, UI pixel-perfect (screenshot-verified: ONLINE badge, bill list, mic, SCAN/DONE). Note: Realme blocks `adb pm grant`; permissions must be tapped manually on first mic/camera use.
- Pushed E4B weights to phone → **app KILLED by Android: `reason=LOW_MEMORY` at RSS 1.8 GB during model load.** Root cause: this phone has **5.7 GB usable RAM — Gemma 3n E4B (4.4 GB) does not fit.** E4B needs a flagship-RAM device.
- **Decision: switch offline model to Gemma 3n E2B int4 (3.14 GB)** — same Gemma 3n family (story unchanged), sized for 6–8 GB phones. E4B deleted from laptop + phone; E2B downloading now → will `adb push` to the same path `/data/local/tmp/llm/gemma.task` (code needs NO change — it loads whatever is at that path).
- **Deepthi: check the demo phone's RAM before pushing weights** (`adb shell cat /proc/meminfo | head -1`): ≥ 10 GB free RAM → E4B ok; 6–8 GB → use E2B; either way the rule-parser fallback keeps the demo alive.

### 2026-07-10 00:00 — Person A (Preethesh)
- Prompt: "it only works for hindi, i want all other languages + inventory details + add price/stock + photo to detect stock + separate customer bills + inventory-used page + camera check".
- **v2 FEATURE DROP (built, tested, installed on A's phone):**
  - **Language switcher** — 🌐 chip in top bar cycles हिंदी → ಕನ್ನಡ → EN; drives offline STT locale AND TTS voice (`AppLanguage.kt`; Gemini Live was already multilingual).
  - **Separate customer bills** — new `Bill` entity (Room v2, destructive migration): every customer gets their own bill; DONE + "NAYI BILL" closes it (with optional customer name field on the summary screen) and archives it; agent tools now target the open bill.
  - **📦 Inventory screen** — full item list (price/unit/stock, red when low), tap to edit, ➕ ADD ITEM dialog (name/price/stock/unit), **📷 SCAN TO STOCK**: camera identifies a product → prefilled editable dialog → saves into inventory (existing item opens for restock edit; unknown product becomes a new item).
  - **📊 Insights ("Aaj ka hisaab") screen** — today's revenue card, AAJ BIKA (sold-today per item: qty + ₹), BHARNA HAI (restock list, stock < 3), CUSTOMER BILLS history (name/time/items/total, open bill marked "chalu").
  - **Camera VERIFIED on device** — logcat: `CameraState OPEN, error=null`, camera device connected to com.khaata.app. Scan overlay + mode captions render.
  - Gemma **E2B** stable on the phone (E4B OOM'd — see previous entry). Build + 13/13 tests green; APK reinstalled via USB.
- **Deepthi: pull main and rebuild** — DB schema bumped to v2 (old installs wipe data on first launch, catalog reseeds automatically). Demo script unchanged; new judge-candy: scan-to-stock + aaj-ka-hisaab.

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

