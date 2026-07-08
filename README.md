# Khaata · खाता

**A local-first Android voice agent that lets any Indian kirana shopkeeper bill customers and track stock just by talking — Hindi, Kannada, or English — working fully offline when connectivity drops.**

Built for the Google DeepMind Bangalore Hackathon (July 11, 2026) by Preethesh (AI pipeline) & Deepthi (UI/DB/build).

## The dual-mode story
- **ONLINE** → **Gemini Live API** via Firebase AI Logic (`firebase-ai`, direct from the app, zero backend): full-duplex audio session with all 7 billing tools declared as `FunctionDeclaration`s.
- **OFFLINE** → **Gemma on-device** (MediaPipe LLM Inference / LiteRT) emits the *same* tool calls — with a deterministic rule-based parser as a safety net, so airplane mode never fails.
- The switch is automatic (`ConnectivityObserver`); the shopkeeper never notices.
- **Omni Flash wildcard**: point the camera at a product → Gemini Flash vision identifies it → same tool pipeline.

## Stack
Kotlin · Jetpack Compose (Material 3) · MVVM + StateFlow · Room · CameraX · on-device SpeechRecognizer + Hindi TTS · Firebase AI Logic (`firebase-ai`, Live API) · MediaPipe `tasks-genai` (Gemma)

## Build
```bash
# any machine with JDK 17 + Android SDK (Deepthi's laptop: toolchain at C:\tools — see progress.md):
set JAVA_HOME=C:\tools\jdk17
gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```
`local.properties` needs `sdk.dir` pointing at your Android SDK. Drop your real Firebase config at `app/google-services.json` (package `com.khaata.app`) for online mode — the plugin is applied conditionally, so the project **builds and runs offline-mode without it** (see `app/google-services.json.example`).

## Repo guide
- `KHAATA_MASTER_PLAN.md` — full build plan, demo script, judge Q&A
- `progress.md` — live shared status + testing guide (read this first)
- `person1.md` / `person2.md` — per-person trackers
- `app/src/main/java/com/khaata/app/` — `agent/` (brains + tools + speech/TTS) · `data/` (Room + catalog) · `ui/` (Compose screens)
- Branch `person-b-deepthi` — full parallel implementation (text-pipeline Gemini fallback), kept as plan-B
