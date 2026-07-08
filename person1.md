# PERSON A (Preethesh) — TASK TRACKER
### Branch: `person-a` | Scope: AI pipeline + agent core (+ Person B's scope if `person-b` branch stays empty)

## Phase 1 — Foundations
- [x] Gradle KTS project scaffold (AGP + Kotlin + Compose + KSP)
- [x] AndroidManifest: mic, camera, internet, network-state permissions
- [x] Firebase AI Logic SDK wired in Gradle (placeholder google-services.json — REPLACE with real one from Firebase console)
- [x] Room DB: Item, BillLine entities + DAOs + AppDatabase
- [x] CatalogSeeder: 50 kirana items (Hindi/Kannada/English names, price, unit, stock)

## Phase 2 — Agent core
- [x] AgentTools.kt — all 7 tools (lookup_price, add_to_bill, update_stock, check_stock, get_summary, remove_last_item, clear_bill)
- [x] CatalogRepository.kt — fuzzy matching (exact → contains → alias map → Levenshtein)
- [x] QuantityParser.kt — Hindi/English number words ("do"=2, "adha"=0.5, "paav"=0.25 …)
- [x] ConnectivityObserver.kt — StateFlow<Boolean>, auto online/offline switch
- [x] LiveApiManager.kt — Gemini Live API session via Firebase AI Logic (function calling, audio conversation)
- [x] OfflineModelManager.kt — Gemma on-device via MediaPipe LLM Inference + deterministic rule-based fallback parser
- [x] OmniFlashManager.kt — camera frame → Gemini Flash vision → product JSON → tool pipeline
- [x] KhaataAgent.kt — orchestrator: mode switch, tool loop, TTS confirmations

## Phase 3 — UI (Person B scope, covered by A if B absent)
- [x] Theme: saffron #FF6B00, dark, high-contrast, 18sp+ fonts
- [x] MainScreen.kt — 3 zones: status badge / animated bill list / big mic button + scan + done
- [x] Animated bill list (slide-in per item), stock-low red badge
- [x] Online/offline badge (green ONLINE / orange OFFLINE)
- [x] CameraScreen.kt — CameraX viewfinder + scan overlay
- [x] SummaryScreen.kt — total, item count, share-as-text
- [x] TtsManager.kt — Hindi TTS confirmations

## Phase 4 — Ship
- [x] `assembleDebug` builds green
- [x] progress.md updated at every step
- [x] Check origin/person-b, merge if present
- [x] Merge person-a → main, push via SSH
