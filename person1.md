# PERSON 1 / PERSON A — PREETHESH (AI Pipeline + Agent Core)
> Your tracker. Update after every session. Deepthi's work is in `person2.md`; shared state in `progress.md`.

## HEADS-UP FROM DEEPTHI (2026-07-08)

I wasn't blocked waiting, so **the full vertical slice — including first versions of ALL your files — is already implemented and compiling** on `main`. You are not starting from zero; you're upgrading working code. Pull `main` first, create your branch:

```bash
git fetch --all
git checkout main && git pull
git checkout -b person-a-preethesh
```

### Your files (all exist, all compile)
| File | What it does now | Your upgrade |
|---|---|---|
| `app/.../agent/KhaataAgent.kt` | Orchestrator: transcript → brain → tool calls → spoken+status reply. Auto-falls back offline if Gemini errors/rate-limits. | Tune replies, edge cases |
| `app/.../agent/AgentTools.kt` | All 7 tools (lookup_price, add_to_bill+update_stock, check_stock, get_summary, remove_last_item, clear_bill) wired to Room | — |
| `app/.../agent/LiveApiManager.kt` | Gemini via **Firebase AI Logic** (`firebase-vertexai`, `gemini-2.0-flash`): transcript → strict-JSON action plan. System prompt included. | **Stretch:** swap to Live API `liveModel` native-audio session + `FunctionDeclaration`s. The `AgentAction` contract stays the same — UI/tools don't change. |
| `app/.../agent/OfflineModelManager.kt` | Gemma on-device via MediaPipe `tasks-genai` (loads `.task` from `/data/local/tmp/llm/`), with deterministic rule-based fallback | Put real weights on the phone; validate Gemma JSON output quality |
| `app/.../agent/OmniFlashManager.kt` | Camera frame → Gemini vision → `{productName, qty, unit, confidence}` JSON → same tool pipeline. Confidence gating 0.7/0.4 in ViewModel. | Prompt tuning with real product packets |
| `app/.../data/CatalogRepository.kt` | Fuzzy match: exact → ~80-entry alias map (Hindi/Kannada/Hinglish) → containment → Levenshtein ≤3 | Add aliases you find failing |
| `app/.../util/ConnectivityObserver.kt` | NetworkCallback → `StateFlow<Boolean>` → `BillingMode.GEMINI_LIVE / GEMMA_OFFLINE` | — |

### Architecture contract (don't break this)
Both brains return the same thing — a JSON action plan parsed by `LiveApiManager.parseActionJson()`:
```json
{"actions":[{"type":"add","item":"Cheeni","qty":2},{"type":"summary"}],"reply":"do kilo cheeni add kiya"}
```
Action types: `add`, `remove_last`, `clear`, `summary`, `check_stock`. Voice, camera, online, offline — everything funnels into `AgentTools`. Keep it that way and integration stays free.

---

## YOUR CRITICAL-PATH TODO (do these before anything else)

- [ ] **Firebase project (30 min):** console.firebase.google.com → create project `khaata` → add Android app with package **`com.khaata.app`** → download `google-services.json` → replace `app/google-services.json` (a placeholder is committed so the build passes; API calls need the real one) → in console enable **Firebase AI Logic** (Build with Gemini). Share the file with Deepthi for her machine.
- [ ] **Gemma weights (start download NOW, ~2GB):** get the LiteRT `.task` build of Gemma E4B (e.g. `gemma-3n-E4B-it-int4.task` from Hugging Face `google/gemma-3n-E4B-it-litert-preview`; E2B also supported). Then:
  ```
  adb shell mkdir -p /data/local/tmp/llm
  adb push gemma-3n-E4B-it-int4.task /data/local/tmp/llm/
  ```
  `OfflineModelManager.MODEL_PATHS` already looks there. App pre-warms it on boot.
- [ ] **ADB test on the demo phone** (this always bites on the day): enable Developer options + USB debugging, `adb devices`, install the APK from `main`.
- [ ] Get an AI Studio API key as backup (aistudio.google.com/apikey).

## SESSION LOG (newest first)
- 2026-07-08 — (Deepthi, on your behalf) All agent files scaffolded + working with Gemini-via-Firebase and rules-fallback offline. Nothing pushed by you yet.
- _add your entries here_
