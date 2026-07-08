# KHAATA — MASTER BUILD PLAN
### Google DeepMind Bangalore Hackathon | July 11, 2026 | WeWork Roshni Tech Hub, Marathahalli
### Team: 2 people | 9:00 AM – 10:00 PM

---

## FIRST — YOU ARE APPROVED. GO WIN IT.

---

## THE ONE-LINE PITCH

> **Khaata — a local-first Android voice agent that lets any Indian shopkeeper
> bill customers and track stock just by talking, in their own language,
> working fully offline when connectivity drops.**

---

## THE INSIGHT NOBODY ELSE WILL HAVE

Every other team will pick ONE technology and build around it.
The winning move is understanding that Google built these five tools
to work as a CHAIN, not a menu. Khaata uses all five — coherently,
not as a checklist.

**The chain:**
```
Shopkeeper speaks
    → [ONLINE]  Gemini Live API  → real-time voice understanding + function calling
    → [OFFLINE] Gemma 4 on-device → local inference, same function calls, no internet
    → Managed Agents              → orchestrates the multi-step billing workflow
    → Antigravity                 → where you BUILD and wire the agent logic
    → Omni Flash                  → camera scan → identify product → add to bill
```

This is the story: **Gemini Live API when connected, Gemma 4 on-device when not.**
The app degrades gracefully and the user never notices the switch.
That dual-mode is the technical story no other team will tell.

---

## WHY THIS WINS FROM THE JUDGE'S SEAT

1. Uses ALL FIVE tracks coherently — not one bolted-on model call
2. Airplane mode demo = proof, not a promise
3. "For India" is literal — 65M+ kirana shops, Hindi/Kannada, tier-2 towns
4. Production-deployable today (Apache 2.0 Gemma 4, free Firebase tier, Room DB)
5. The demo is 30 seconds and self-explanatory — no explanation needed

---

## TECH STACK (DECIDED, NOT NEGOTIABLE)

### Android App
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + StateFlow + ViewModel
- **Database:** Room (SQLite ORM) — local, offline, instant
- **Audio:** Android AudioRecord API (PCM 16kHz mono — matches Live API spec)
- **TTS (offline):** Android TextToSpeech API (built-in, no dependency)
- **Build:** Gradle KTS

### AI — Online Mode (when wifi/data available)
- **Model:** Gemini 2.5 Flash Live (`gemini-2.5-flash-native-audio-preview-12-2025`)
- **SDK:** Firebase AI Logic Android SDK (direct from app, no backend needed)
- **Features used:** Real-time audio streaming, function calling, multilingual (Hindi/Kannada)
- **Transport:** WebSocket via Firebase (handled by SDK, not raw WebSocket)

### AI — Offline Mode (airplane mode / no internet)
- **Model:** Gemma 4 E4B (4B params, ~2GB INT4 quantized)
- **Runtime:** Google AI Edge / LiteRT-LM
- **Features used:** On-device inference, native function calling, Agent Skills
- **Why E4B not E2B:** E4B handles multi-item parsing + reasoning better; fits on 4GB RAM phones

### Agent Orchestration
- **Managed Agents:** Orchestrates the multi-step billing workflow (hear → parse → lookup → bill → stock)
- **Antigravity:** Where you build and wire the agent locally during development
- **Function tools:** `lookup_price()`, `add_to_bill()`, `update_stock()`, `check_stock()`, `clear_bill()`

### Vision (the wildcard)
- **Omni Flash:** Camera → product identification → auto-add to bill
- **How:** CameraX captures frame → send to Omni Flash API → returns product name + quantity → same tool pipeline

### Scaffolding
- **App shell:** AI Studio (Kotlin + Jetpack Compose, mic + camera access in minutes)
- **Export to:** Antigravity for agent wiring

### Pre-loaded Data
- **Catalog:** 50 common kirana items hardcoded in Room DB (items, prices, stock count)
- **Format:** `Item(id, nameHindi, nameKannada, nameEnglish, unitPrice, unit, stockQty)`
- **Items include:** Atta (Aashirvaad, Pillsbury), Cheeni, Dal (Toor, Moong, Chana), Rice, Oil (Saffola, Sunflower), Parle-G, Maggi, Dairy Milk, Horlicks, Surf Excel, Vim, Colgate, Lifebuoy, Dettol, Hajmola, Pan Masala, Bread, Eggs, Milk, Tea (Tata, Red Label)

---

## ARCHITECTURE DIAGRAM

```
┌─────────────────────────────────────────────────────────────┐
│                    KHAATA ANDROID APP                        │
│                                                             │
│  ┌──────────┐    ┌────────────────┐    ┌─────────────────┐  │
│  │  MIC UI  │    │   CAMERA UI    │    │    BILL UI      │  │
│  │ (big btn)│    │ (Omni Flash)   │    │ (animated list) │  │
│  └────┬─────┘    └───────┬────────┘    └────────┬────────┘  │
│       │                  │                       │           │
│  ┌────▼──────────────────▼───────────────────────▼────────┐  │
│  │              AGENT ORCHESTRATOR (Managed Agents)        │  │
│  │                                                         │  │
│  │  Tools: lookup_price | add_to_bill | update_stock       │  │
│  │         check_stock  | clear_bill  | get_summary        │  │
│  └────────────────────────┬────────────────────────────────┘  │
│                           │                                   │
│              ┌────────────▼────────────┐                      │
│              │    CONNECTIVITY CHECK    │                      │
│              └──────┬───────────┬───────┘                      │
│                     │           │                              │
│           ONLINE    │           │  OFFLINE                    │
│                     ▼           ▼                              │
│          ┌──────────────┐  ┌──────────────┐                   │
│          │ Gemini Live  │  │  Gemma 4 E4B │                   │
│          │ API (Firebase│  │  on-device   │                   │
│          │ AI Logic)    │  │  (LiteRT-LM) │                   │
│          └──────┬───────┘  └──────┬───────┘                   │
│                 │                 │                            │
│          ┌──────▼─────────────────▼───────┐                   │
│          │         ROOM DATABASE           │                   │
│          │    (catalog + bill + stock)     │                   │
│          └────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

---

## TEAM SPLIT — WHO DOES WHAT

### PERSON 1 (Preethesh) — AI Pipeline + Agent Core
You own everything that makes Khaata intelligent.

**Your tasks:**
- [ ] Set up Firebase project + Firebase AI Logic SDK in the Android app
- [ ] Wire Gemini Live API: AudioRecord → PCM stream → Firebase LiveModel → function calls
- [ ] Set up Gemma 4 E4B on-device via LiteRT-LM (offline mode)
- [ ] Implement ConnectivityManager listener → auto-switch online/offline mode
- [ ] Define all 6 agent tool functions in Kotlin
- [ ] Wire Managed Agents orchestration layer
- [ ] Implement fuzzy catalog matching (handles "Parle" → "Parle-G", "cheeni" → "Cheeni")
- [ ] Implement Omni Flash camera → product identification → tool call
- [ ] Handle edge cases (see Edge Cases section)
- [ ] System prompt engineering for the billing agent

**Your files:**
- `KhaataAgent.kt` — the core agent class
- `AgentTools.kt` — all tool function definitions
- `LiveApiManager.kt` — Gemini Live API connection management
- `OfflineModelManager.kt` — Gemma 4 LiteRT-LM wrapper
- `CatalogRepository.kt` — Room DB queries + fuzzy matching
- `OmniFlashManager.kt` — camera frame → product ID

---

### PERSON 2 — Android UI + Room DB + Demo Polish
You own everything the kirana wallah sees and touches.

**Your tasks:**
- [ ] Generate app scaffold in AI Studio (Kotlin + Compose + mic + camera access)
- [ ] Export to Antigravity, set up local dev environment
- [ ] Build Room DB: entities (Item, BillLine, Transaction), DAOs, Database class
- [ ] Seed the 50-item catalog (with Hindi/Kannada names, prices, stock)
- [ ] Build the main screen UI (see UI spec below)
- [ ] Build the animated bill list (each item slides in as agent adds it)
- [ ] Build the online/offline mode badge (green = online, orange = offline)
- [ ] Build stock warning UI (red badge when stock < 3)
- [ ] Build the camera scan UI (CameraX viewfinder + scan overlay)
- [ ] TTS integration for voice confirmation responses
- [ ] Bill summary screen (total, item count, print/share as text)
- [ ] Rehearse and own the demo script

**Your files:**
- `MainActivity.kt` + `MainScreen.kt` — main Compose screen
- `BillScreen.kt` — animated bill list
- `CameraScreen.kt` — Omni Flash scan UI
- `AppDatabase.kt` — Room DB setup
- `ItemDao.kt`, `BillDao.kt` — Room DAOs
- `CatalogSeeder.kt` — pre-seeds the 50 items on first launch
- `SummaryScreen.kt` — end-of-bill total screen

---

## UI SPEC — DESIGNED FOR THE KIRANA WALLAH

**Rule: if a 60-year-old shopkeeper with no smartphone experience can't use it in 10 seconds, redesign it.**

### Main Screen (3 zones, full screen)
```
┌─────────────────────────────┐
│  🟢 ONLINE   [KHAATA]  [🗑️]  │  ← status badge + app name + clear bill
├─────────────────────────────┤
│                             │
│   BILL (animated list)      │
│   ─────────────────────     │
│   ✅ Cheeni 2kg    ₹80     │  ← slides in line by line
│   ✅ Parle-G 1pkt  ₹10     │
│   ✅ Maggi 3pkt    ₹45     │
│                             │
│   TOTAL: ₹135              │
│                             │
├─────────────────────────────┤
│                             │
│     🎤  (BIG PULSING)      │  ← tap and hold to speak
│    "Bolo apna order"        │
│                             │
│  [📷 SCAN]    [✅ DONE]    │  ← camera scan + finish bill
└─────────────────────────────┘
```

**Design rules:**
- Font size minimum 18sp everywhere, 24sp for bill items
- Colors: Deep saffron (#FF6B00) as primary, white text, dark background
- Mic button: 80dp diameter, pulses orange when listening
- Every item addition = slide-in animation + soft chime sound
- Hindi text labels alongside English
- High contrast — works in bright sunlight at a shop counter

---

## AGENT SYSTEM PROMPT

```
You are Khaata, a billing assistant for small Indian shops.
Your job: listen to the shopkeeper's order in Hindi, Kannada, or English,
identify items and quantities, and update the bill.

RULES:
- Only call tools — never respond with text during billing
- Parse quantities from Hindi ("do" = 2, "teen" = 3, "ek" = 1, "char" = 4, "paanch" = 5, "adha" = 0.5)
- Match items fuzzily — "Parle" → Parle-G, "Maggi" → Maggi Noodles, "namak" → Salt
- If an item is unclear, call check_stock with your best guess and confirm
- When the shopkeeper says "khatam", "done", "bas", or "total" — call get_summary
- When the shopkeeper says "hatao" or "remove" — call remove_last_item
- Always confirm each addition by speaking the item and price back

LANGUAGE: Respond in whatever language the shopkeeper is using.
DEFAULT: Hindi
```

---

## AGENT TOOL DEFINITIONS

```kotlin
// AgentTools.kt

data class Item(
    val id: Int,
    val nameHindi: String,
    val nameKannada: String,
    val nameEnglish: String,
    val unitPrice: Double,
    val unit: String,        // "kg", "pkt", "piece", "litre"
    val stockQty: Double
)

// Tool 1: Look up item price and availability
fun lookup_price(itemName: String, quantity: Double): LookupResult {
    val item = catalog.fuzzyMatch(itemName)  // fuzzy match on all name fields
    return LookupResult(
        found = item != null,
        itemName = item?.nameHindi,
        unitPrice = item?.unitPrice,
        totalPrice = item?.unitPrice?.times(quantity),
        stockAvailable = (item?.stockQty ?: 0.0) >= quantity,
        stockRemaining = item?.stockQty
    )
}

// Tool 2: Add confirmed item to current bill
fun add_to_bill(itemId: Int, quantity: Double, totalPrice: Double): BillResult {
    db.billDao().insert(BillLine(itemId, quantity, totalPrice))
    return BillResult(success = true, billTotal = db.billDao().getTotal())
}

// Tool 3: Decrement stock after adding to bill
fun update_stock(itemId: Int, quantitySold: Double): StockResult {
    val newStock = db.itemDao().decrementStock(itemId, quantitySold)
    return StockResult(newStockLevel = newStock, lowStockWarning = newStock < 3)
}

// Tool 4: Check current stock without modifying
fun check_stock(itemName: String): StockCheckResult {
    val item = catalog.fuzzyMatch(itemName)
    return StockCheckResult(item?.stockQty, item?.unit)
}

// Tool 5: Get current bill summary
fun get_summary(): SummaryResult {
    val lines = db.billDao().getCurrentBill()
    return SummaryResult(items = lines, total = lines.sumOf { it.totalPrice })
}

// Tool 6: Remove last item (correction flow)
fun remove_last_item(): RemoveResult {
    val removed = db.billDao().removeLastItem()
    db.itemDao().incrementStock(removed.itemId, removed.quantity)  // restore stock
    return RemoveResult(removedItem = removed.itemName, newTotal = db.billDao().getTotal())
}

// Tool 7: Clear entire bill (new customer)
fun clear_bill(): ClearResult {
    db.billDao().clearCurrentBill()
    return ClearResult(success = true)
}
```

---

## FUZZY MATCHING LOGIC (critical for real-world use)

```kotlin
// CatalogRepository.kt

fun fuzzyMatch(input: String): Item? {
    val normalized = input.lowercase().trim()

    // 1. Exact match first (fastest)
    catalog.find { it.nameHindi.lowercase() == normalized ||
                   it.nameKannada.lowercase() == normalized ||
                   it.nameEnglish.lowercase() == normalized }
        ?.let { return it }

    // 2. Contains match
    catalog.find { normalized.contains(it.nameHindi.lowercase()) ||
                   it.nameHindi.lowercase().contains(normalized) ||
                   normalized.contains(it.nameEnglish.lowercase()) }
        ?.let { return it }

    // 3. Alias map (hardcoded common substitutions)
    val aliases = mapOf(
        "parle" to "Parle-G",
        "gluco" to "Parle-G",
        "maggi" to "Maggi Noodles",
        "noodles" to "Maggi Noodles",
        "cheeni" to "Cheeni",
        "sugar" to "Cheeni",
        "namak" to "Salt",
        "tel" to "Cooking Oil",
        "atta" to "Wheat Flour",
        "maida" to "Maida",
        "doodh" to "Milk",
        "chai" to "Tea",
        "sabun" to "Soap",
        "toothpaste" to "Colgate"
    )
    aliases[normalized]?.let { aliasName ->
        return catalog.find { it.nameEnglish == aliasName }
    }

    // 4. Levenshtein distance fallback (for typos/mishears)
    return catalog.minByOrNull { levenshtein(normalized, it.nameHindi.lowercase()) }
        ?.takeIf { levenshtein(normalized, it.nameHindi.lowercase()) <= 3 }
}
```

---

## ONLINE / OFFLINE AUTO-SWITCH

```kotlin
// ConnectivityManager.kt

class ConnectivityManager(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: StateFlow<Boolean> = // observe network callbacks
        // emit true when network available, false when lost

    fun getActiveMode(): BillingMode {
        return if (isOnline.value) BillingMode.GEMINI_LIVE else BillingMode.GEMMA_OFFLINE
    }
}

// In KhaataAgent.kt:
fun startListening() {
    when (connectivityManager.getActiveMode()) {
        BillingMode.GEMINI_LIVE -> liveApiManager.startSession()
        BillingMode.GEMMA_OFFLINE -> offlineModelManager.startInference()
    }
}
```

---

## OMNI FLASH INTEGRATION (the wildcard demo moment)

```kotlin
// OmniFlashManager.kt

// Shopkeeper points camera at a product
// Omni Flash identifies it → returns structured data → same tool pipeline

suspend fun identifyProduct(imageBytes: ByteArray): ProductIdentification {
    val response = omniFlashApi.analyze(
        image = imageBytes,
        prompt = """
            Look at this product. Return JSON only:
            {
                "productName": "common name in Hindi",
                "quantity": 1,
                "unit": "pkt/kg/piece/litre",
                "confidence": 0.0-1.0
            }
            If you cannot identify the product, return confidence: 0.0
        """
    )
    return parseJson(response)
}

// If confidence > 0.7: auto-add to bill
// If confidence 0.4-0.7: show preview, ask shopkeeper to confirm
// If confidence < 0.4: show "couldn't identify, please speak the item"
```

---

## EDGE CASES — HANDLED, NOT HOPED

| Edge Case | What happens | How we handle it |
|-----------|-------------|------------------|
| "do nahi teen Maggi" (correction) | Remove last item, add correct qty | `remove_last_item()` tool |
| Item not in catalog | Agent says "yeh item nahi mila, dobara boliye" | fuzzyMatch returns null → voice prompt |
| Stock = 0 for requested item | Warning before adding | `stockAvailable = false` in LookupResult |
| Code-mixed input "2 packet Parle aur ek Dairy Milk" | Numbers in English parsed | Number normalization in pre-processing |
| Gemini Live API rate limit hit | Auto-switch to offline Gemma 4 | ConnectivityManager + mode switch |
| App killed mid-bill | Bill state persists in Room DB | Room auto-saves every insertion |
| Shopkeeper speaks too fast | Barge-in handling | Gemini Live API supports barge-in natively |
| Wrong item scanned with camera | Low confidence → ask to confirm | Confidence threshold gating |
| Multiple customers back-to-back | "nayi bill" / "new bill" command | `clear_bill()` tool |
| Quantity in fractions ("adha kilo") | 0.5 parsed correctly | Quantity parser: "adha"→0.5, "paav"→0.25 |
| Battery/power cut mid-session | Last bill state restored | Room DB persistence |
| Venue wifi dead entirely | Full offline mode | Gemma 4 E4B on-device, zero dependency |

---

## QUANTITY PARSER

```kotlin
val hindiNumbers = mapOf(
    "ek" to 1.0, "do" to 2.0, "teen" to 3.0, "char" to 4.0,
    "paanch" to 5.0, "che" to 6.0, "saat" to 7.0, "aath" to 8.0,
    "nau" to 9.0, "das" to 10.0, "adha" to 0.5, "paav" to 0.25,
    "dedh" to 1.5, "dhai" to 2.5, "one" to 1.0, "two" to 2.0,
    "three" to 3.0, "half" to 0.5, "quarter" to 0.25
)
// Also parse "2", "3", "1.5" as Doubles directly
```

---

## HOUR-BY-HOUR DAY PLAN — JULY 11

```
09:00 AM  Arrive, check in, find seats together, set up laptops
09:15 AM  Quick sync: confirm task split, share Firebase project access,
           confirm Gemma 4 weights are on Person 1's machine
09:30 AM  START BUILDING

── PHASE 1: FOUNDATIONS (9:30 – 12:00) ──────────────────────────────

Person 1:
09:30  Firebase project setup + Firebase AI Logic SDK added to Gradle
10:00  LiveModel initialized, test: mic → Gemini Live → text response in logs
10:30  First function call working: speak "ek Parle-G" → lookup_price called in logs
11:00  add_to_bill + update_stock tools working in tool call loop
11:30  ConnectivityManager: auto-switch online/offline confirmed working
12:00  CHECKPOINT: full online tool call loop working end-to-end

Person 2:
09:30  AI Studio: generate Kotlin + Compose app with mic + camera permissions
10:00  Export to Antigravity, get it running on demo phone via ADB
10:30  Room DB: entities + DAOs + AppDatabase created
11:00  CatalogSeeder: 50 items seeded, verify with DB browser
11:30  Main screen UI shell: 3 zones (status bar, bill list, mic button)
12:00  CHECKPOINT: app launches on phone, shows bill UI, DB has items

── LUNCH (12:00 – 12:30) — EAT, DON'T SKIP ─────────────────────────

── PHASE 2: INTEGRATION (12:30 – 15:30) ─────────────────────────────

Person 1:
12:30  Offline mode: Gemma 4 E4B loaded via LiteRT-LM, test inference
13:00  Offline function calling working (same tool definitions, different model)
13:30  Fuzzy matching: test "Parle", "cheeni", "tel" all resolve correctly
14:00  Omni Flash: camera frame → product JSON response working
14:30  System prompt tuned: Hindi numbers parsed, correction flow tested
15:00  Full agent: speak 5-item order → all tools called correctly
15:30  CHECKPOINT: full end-to-end loop working (messy but functional)

Person 2:
12:30  Animated bill list: items slide in as agent adds them
13:00  Online/offline badge: green "ONLINE" / orange "OFFLINE" indicator
13:30  Stock warning UI: red badge appears when stock < 3
14:00  Camera scan UI: CameraX viewfinder + scan button + overlay
14:30  TTS: agent responses spoken back out loud in Hindi
15:00  Bill summary screen: total, item count, WhatsApp share button
15:30  CHECKPOINT: UI complete, animations smooth, TTS working

── PHASE 3: POLISH + REHEARSAL (15:30 – 18:00) ──────────────────────

15:30  Integration: connect Person 1's agent to Person 2's UI
16:00  Full end-to-end test: speak order → bill builds → stock updates → TTS confirms
16:30  Airplane mode test: confirm offline mode works fully
17:00  Edge case testing: corrections, unknown items, camera scan
17:30  Bug fixes (save 30 min buffer here — you will need it)
18:00  CHECKPOINT: demo-ready product

── PHASE 4: DEMO PREP (18:00 – JUDGING) ─────────────────────────────

18:00  Rehearse demo 3 times minimum — Person 2 owns the script
18:30  Pre-seed a clean demo state (fresh catalog, zero bills)
19:00  Charge all devices to 100%, test ADB one more time
19:30  Prepare talking points for judge Q&A (see below)
20:00  Rest, eat, relax — you've built it
20:00+ JUDGING — go win it
```

---

## THE 30-SECOND DEMO SCRIPT (Person 2 delivers this)

> "This is a kirana shop in tier-2 India. No English. Patchy internet. Paper billing."

*[Show phone. Put it in airplane mode. Hold it up so judges can see the OFFLINE badge.]*

> "The shopkeeper just talks."

*[Tap mic. Speak clearly:]*
> **"Do kilo cheeni, ek Parle-G, teen Maggi."**

*[Wait. Bill builds line by line. Stock ticks down. TTS confirms each item.]*

> "Itemized bill. Stock updated. All on-device. Zero internet."

*[Point camera at a product packet.]*

> "Or just scan."

*[Product auto-identifies and adds to bill.]*

> "65 million kirana shops in India. None of them have something like this."

*[Show total. Done.]*

---

## JUDGE Q&A — PREP ANSWERS

**"Why not just use a cloud chatbot?"**
> "Venue wifi in a tier-2 market lane is not guaranteed. And these shops handle cash — the shopkeeper doesn't want billing data leaving their device. Gemma 4 on-device gives you both: it works offline and it's private by construction."

**"What makes this different from existing POS apps?"**
> "Every existing POS app requires you to tap through a screen. A kirana wallah serves 5 customers at once — their hands aren't free. Voice is the natural interface. Nobody built it because it required on-device multilingual voice AI, which only became viable with Gemma 4."

**"How does it handle dialects and code-switching?"**
> "Gemini Live API supports 70 languages and handles code-mixed input naturally. Gemma 4 supports 140+ languages on-device. The fuzzy matcher handles common aliases and mishears. In the demo we tested Hinglish mixing and it resolves correctly."

**"What's the path to production?"**
> "Gemma 4 is Apache 2.0 — commercially deployable, no royalties. The app installs from the Play Store. The catalog is configurable per shop. We could deploy this to a pilot of 100 shops in Mangalore tomorrow."

---

## PRE-HACKATHON CHECKLIST (DO BEFORE JULY 11)

### Person 1 — Do this week
- [ ] Download Gemma 4 E4B quantized weights (INT4, ~2GB) via `ollama pull gemma4:4b`
- [ ] Test Gemma 4 function calling locally on your laptop first
- [ ] Set up Firebase project, enable Firebase AI Logic, get the `google-services.json`
- [ ] Test Gemini Live API: run the Firebase Android quickstart, confirm audio streams
- [ ] Get Google AI Studio API key for Omni Flash
- [ ] Test ADB connection to your demo Android phone (must be Snapdragon for NPU)
- [ ] Read: `https://developer.android.com/ai/gemini/live` (20 min, it's clean)
- [ ] Read: `https://firebase.google.com/docs/ai-logic/live-api` (setup steps)

### Person 2 — Do this week
- [ ] Install Antigravity VS Code extension
- [ ] Create a blank Android project in AI Studio, confirm it scaffolds + runs on phone
- [ ] Familiarize yourself with Jetpack Compose basics (LazyColumn for the bill list)
- [ ] Set up CameraX in a test app, confirm camera permission + preview works
- [ ] Test Android TTS in Hindi: `textToSpeech.language = Locale("hi", "IN")`
- [ ] Write the 50-item catalog as a Kotlin list (do this in advance, it's boring but essential)
- [ ] Design the UI in Figma or paper first — 30 min now saves 2 hours on the day

### Both — Night before (July 10)
- [ ] Charge all devices to 100%
- [ ] Pack: laptop charger, USB-C cables, power strip (venue won't have enough outlets), personal hotspot, phone stands
- [ ] Pre-load Gemma 4 weights onto the demo phone if possible
- [ ] Set departure alarm for 7:30 AM — Marathahalli at 9 AM means leaving by 8:00 at latest given Bangalore traffic
- [ ] Pack food/snacks — hackathon food is unpredictable

---

## WHAT TO MENTION AS ROADMAP (don't build, do mention)

- Multi-language UI (Kannada interface, not just Kannada voice)
- Customer credit tracking ("Raju ne abhi nahi diya")
- Daily/weekly sales summary
- WhatsApp bill sharing to customer
- Multi-device sync (owner's phone + assistant's tablet)
- Integration with UPI for payment confirmation
- Supplier order auto-generation when stock runs low

---

## THE HONEST RISKS AND HOW YOU'RE HANDLING THEM

| Risk | Severity | Your mitigation |
|------|----------|----------------|
| Gemini Live API preview instability | Medium | Offline Gemma 4 fallback is fully functional |
| Venue wifi failure | High | Offline mode is the demo — airplane mode is intentional |
| On-device model too slow on demo phone | Medium | E4B on Snapdragon NPU is fast; pre-warm on boot |
| Noisy venue during voice demo | High | Demo in a corner; tap-to-add as fallback |
| ADB connection drops during demo | Low | Pre-install APK, don't rely on live ADB during judging |
| Fuzzy match fails on unusual item name | Low | Catalog covers 50 common items; edge cases say "item not found" gracefully |

---

## THE ONE-SENTENCE REASON YOU WIN

You're not building an AI app that happens to be for India.
You're building for India using AI the way only these five tools — chained together — make possible.

That's the difference. Go cook.
