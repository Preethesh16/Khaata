# Khaata · खाता

**A local-first Android voice agent that lets any Indian kirana shopkeeper bill customers and track stock just by talking — Hindi, Kannada, or English — working fully offline when connectivity drops.**

Built for the Google DeepMind Bangalore Hackathon (July 11, 2026) by Preethesh (AI pipeline) & Deepthi (UI/DB/build).

## The dual-mode story
- **ONLINE** → Gemini via **Firebase AI Logic** (direct from the app, zero backend) parses speech into tool calls.
- **OFFLINE** → **Gemma on-device** (MediaPipe LLM Inference / LiteRT) emits the *same* tool calls — with a deterministic rule-based parser as a safety net, so airplane mode never fails.
- The switch is automatic (`ConnectivityObserver`); the shopkeeper never notices.
- **Omni Flash wildcard**: point the camera at a product → Gemini vision identifies it → same tool pipeline.

## Stack
Kotlin · Jetpack Compose (Material 3) · MVVM + StateFlow · Room · CameraX · Android SpeechRecognizer (offline-preferring) + TTS · Firebase AI Logic (`firebase-vertexai`) · MediaPipe `tasks-genai` (Gemma)

## Build
```bash
# Windows (toolchain at C:\tools — see progress.md):
set JAVA_HOME=C:\tools\jdk17
gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```
`local.properties` needs `sdk.dir` pointing at your Android SDK. Replace `app/google-services.json` with your real Firebase config (package `com.khaata.app`) for online mode; offline mode works without it.

## Repo guide
- `KHAATA_MASTER_PLAN.md` — full build plan, demo script, judge Q&A
- `progress.md` — live shared status (read this first)
- `person1.md` / `person2.md` — per-person trackers
- `app/src/main/java/com/khaata/app/` — `agent/` (brains + tools) · `data/` (Room + catalog) · `ui/` (Compose) · `util/` (speech, TTS, connectivity, quantities)
