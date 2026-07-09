# 🧾 Khaata

> **A local-first Android voice agent that lets any Indian shopkeeper bill customers and track
> stock just by talking, in their own language — working fully offline when connectivity drops.**

Built for the **Google DeepMind Bangalore Hackathon** (July 11, 2026) by
**Preethesh** (`person-a`) and **Deepthi** (`person-b`).

## The dual-mode story

```
Shopkeeper speaks
    → [ONLINE]  Gemini Live API (Firebase AI Logic) → real-time voice + function calling
    → [OFFLINE] Gemma on-device (LiteRT / MediaPipe) → same function calls, zero internet
    → 7 shared agent tools → Room DB (catalog + bill + stock)
    → Omni Flash: camera scan → Gemini Flash vision → same tool pipeline
```

The app watches connectivity and switches brains automatically. The shopkeeper never notices.
Airplane mode **is** the demo.

## Project layout

| Area | Files |
|---|---|
| Agent core | `app/src/main/java/com/khaata/app/agent/` — `KhaataAgent` (orchestrator), `LiveApiManager` (Gemini Live), `OfflineModelManager` (Gemma + rule fallback), `OmniFlashManager` (vision), `AgentTools` (7 tools), `QuantityParser`, `ConnectivityObserver`, `SpeechInputManager`, `TtsManager` |
| Data | `app/src/main/java/com/khaata/app/data/` — Room entities/DAOs, `CatalogSeeder` (50 kirana items, hi/kn/en), `CatalogRepository` (fuzzy match: exact → contains → aliases → Levenshtein) |
| UI | `app/src/main/java/com/khaata/app/ui/` — `MainScreen` (badge, animated bill, pulsing mic), `CameraScreen` (CameraX + confirm dialog), `SummaryScreen` (share bill) |
| Tests | `app/src/test/` — quantity parser + fuzzy matching unit tests |

## Setup (both machines)

1. **Firebase** — [console.firebase.google.com](https://console.firebase.google.com) → project → add
   Android app `com.khaata.app` → enable **Firebase AI Logic** → put `google-services.json` at
   `app/google-services.json`. The build works without it; online mode doesn't.
2. **Gemma weights (offline brain)** — download the Gemma 3n E4B LiteRT `.task` bundle (~2 GB), then
   `adb push gemma.task /data/local/tmp/llm/gemma.task`. Without weights the offline mode still
   works via the deterministic rule parser.
3. **Offline speech packs** — demo phone: Settings → Google → Voice → Offline speech recognition →
   download Hindi + English (India).
4. **Build & install** — `./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk`
5. **Unit tests** — `./gradlew testDebugUnitTest`

## The 30-second demo

1. Airplane mode ON — badge shows 🟠 OFFLINE.
2. Tap mic: **"Do kilo cheeni, ek Parle-G, teen Maggi."**
3. Bill builds line by line, stock ticks down, TTS confirms in Hindi.
4. "Itemized bill. Stock updated. All on-device. Zero internet."
5. Back online → point camera at a product → auto-identifies and adds.
6. "Khatam" → total. *65 million kirana shops in India. None of them have this.*

Tip: the ↺ button in the top bar resets to a clean demo state (fresh stock, empty bill).

## Team workflow

- `person-a` = Preethesh (agent/AI pipeline) · `person-b` = Deepthi (UI/demo polish)
- Every prompt, change, and merge is logged in [`progress.md`](progress.md). Read it first.
- Task trackers: [`person1.md`](person1.md), [`person2.md`](person2.md)
