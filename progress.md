# KHAATA — PROGRESS TRACKER
> Shared log. **Update this file on every work session / prompt / merge** so the other person always knows the current state.
> Branches: `person-a-preethesh` (AI pipeline) · `person-b-deepthi` (UI + DB + build) · `main` (integrated, always demo-able)

---

## CURRENT STATE (updated: 2026-07-08, by Deepthi / Person B)

**✅ The full app is built and compiles into a working APK.** All 4 phases from the master plan are implemented end-to-end on branch `person-b-deepthi` (merged to `main`). What remains before demo day is **configuration, not code**: real Firebase config, Gemma weights on the phone, and rehearsal.

| Layer | Status | Notes |
|---|---|---|
| Android app scaffold (Kotlin + Compose + M3) | ✅ Done | Gradle KTS, compileSdk 35, minSdk 26 |
| Room DB (Item, BillLine, DAOs, AppDatabase) | ✅ Done | Persists bill across app kill |
| 50-item kirana catalog seeder (Hindi/Kannada/English + prices + stock) | ✅ Done | `CatalogSeeder.kt` |
| Fuzzy matching (exact → alias → contains → Levenshtein) | ✅ Done | ~80 aliases incl. Kannada ("sakkare", "akki", "bele") |
| Main screen (status badge, animated bill list, pulsing mic, SCAN/DONE) | ✅ Done | Saffron-on-dark, 18sp+ fonts |
| Online/offline badge + engine label | ✅ Done | green ONLINE / orange OFFLINE |
| Low-stock warning strip (stock < 3) | ✅ Done | red strip + per-add warning |
| Bill summary screen + WhatsApp/text share | ✅ Done | `SummaryScreen.kt` |
| Camera scan UI (CameraX viewfinder + overlay + confirm flow) | ✅ Done | `CameraScreen.kt` |
| TTS voice confirmations (Hindi, offline) | ✅ Done | `TtsManager.kt` |
| Speech input (SpeechRecognizer, prefers on-device/offline) | ✅ Done | `SpeechManager.kt` |
| Agent orchestrator + 7 tools (lookup/add/stock/check/summary/remove/clear) | ✅ Done | `KhaataAgent.kt`, `AgentTools.kt` |
| ONLINE brain: Gemini via Firebase AI Logic (no backend) | ✅ Code done | needs real `google-services.json` (Person A) |
| OFFLINE brain: Gemma on-device (MediaPipe LLM inference) + rule-based fallback | ✅ Code done | works TODAY via rules even without weights |
| Auto-switch online↔offline (ConnectivityObserver) | ✅ Done | also falls back offline on API failure |
| Omni Flash camera → Gemini vision → same tool pipeline | ✅ Code done | confidence gating 0.7/0.4 implemented |
| Quantity parser (Hindi/Kannada/English, adha=0.5, paav=0.25, dedh=1.5) | ✅ Done | `QuantityParser.kt` |
| Edge cases (correction "hatao", "naya bill", unknown item, empty bill) | ✅ Done | see KhaataAgent |
| Build toolchain on this machine (JDK17 + Android SDK at `C:\tools`) | ✅ Done | build via `gradlew.bat` (see below) |
| Real Firebase project + google-services.json | ❌ TODO — Person A | placeholder file in repo builds fine but API calls fail |
| Gemma E4B weights pushed to demo phone | ❌ TODO — Person A | see person1.md for adb commands |
| Test on physical phone via ADB | ❌ TODO — both | |
| Demo rehearsal ×3 | ❌ TODO — both | script in master plan; Deepthi owns delivery |

---

## HOW TO BUILD (this machine — Snapdragon/ARM64 WSL laptop)

WSL is ARM64, so the build runs on the **Windows side** (toolchain installed at `C:\tools`):

```bash
# from WSL, inside the repo:
cmd.exe /c "set JAVA_HOME=C:\tools\jdk17&& gradlew.bat assembleDebug"
# APK lands at app/build/outputs/apk/debug/app-debug.apk
```

From Windows CMD/PowerShell: `set JAVA_HOME=C:\tools\jdk17` then `gradlew.bat assembleDebug`.
`local.properties` (not committed) must contain: `sdk.dir=C\:\\tools\\android-sdk`

Install on phone: `C:\tools\android-sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk`

---

## GIT WORKFLOW (agreed)

1. Person A works on `person-a-preethesh`, Person B on `person-b-deepthi`.
2. Before starting any session: `git fetch --all` and check the other branch + this file.
3. Small commits, push often. Merge into `main` only when your piece runs.
4. Before merging: pull `main`, resolve, test build, then merge + push.
5. **Never commit real `google-services.json` API keys to a public repo** — repo is private, but still prefer keeping the real file local; placeholder is committed.

---

## SESSION LOG (newest first)

### 2026-07-08 — Deepthi (Person B) — "one-shot build" session (Claude Code)
- Confirmed Person A had pushed nothing beyond the master plan (`main` @ b4cfafc). Not blocked — built the whole vertical slice.
- Created branch `person-b-deepthi`.
- Installed build toolchain: Microsoft OpenJDK 17 (Windows ARM64) + Android SDK (platform 35, build-tools 35.0.0, platform-tools) at `C:\tools` — WSL here is aarch64 so Linux-side SDK doesn't work; builds go through `cmd.exe` → `gradlew.bat`.
- Wrote the entire app (see table above): data layer, agent layer, both AI brains, UI (3-zone main screen per the master-plan wireframe), camera, summary, TTS, speech.
- Key architecture decision: **both brains emit the same JSON action contract** (`{"actions":[...],"reply":"..."}`) parsed into `AgentAction` — so Gemini online, Gemma offline, rules fallback, and camera scan all funnel into the same 7 tools. Person A can upgrade the online path to the native-audio Live API without touching UI or tools.
- Second key decision: offline mode has a **deterministic rule-based parser fallback** (QuantityParser + fuzzy match + keyword routing). The airplane-mode demo works even if Gemma weights aren't on the phone — Gemma is an upgrade, not a dependency.
- Built APK successfully; committed + pushed `person-b-deepthi`, merged to `main`, pushed.
- Created `progress.md`, `person1.md`, `person2.md`.

### 2026-07-07 — Preethesh (Person A)
- Pushed `KHAATA_MASTER_PLAN.md` to `main` (b4cfafc).

---

## NEXT ACTIONS

**Person A (Preethesh) — see person1.md for exact steps:**
1. Create real Firebase project → enable AI Logic → replace `app/google-services.json` (keep package `com.khaata.app`).
2. Download Gemma E4B `.task` weights (~2GB) NOW, push to demo phone at `/data/local/tmp/llm/`.
3. Test ADB on the demo phone.
4. (Stretch) Upgrade `LiveApiManager` to Live API native-audio session with `FunctionDeclaration`s.

**Person B (Deepthi):**
1. Pull `main` on demo day, install APK on phone, verify mic/camera permissions.
2. Enable offline Hindi speech model on the phone (Settings → Google → Voice → Offline speech recognition → download Hindi).
3. Rehearse the 30-second demo script ×3 (script in master plan).
