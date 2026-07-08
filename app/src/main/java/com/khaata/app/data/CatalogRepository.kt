package com.khaata.app.data

/**
 * Catalog lookups + fuzzy matching.
 * Handles "Parle" -> Parle-G, "cheeni"/"sugar" -> Cheeni, "tel" -> oil, mishears via Levenshtein.
 */
class CatalogRepository(private val db: AppDatabase) {

    private var cache: List<Item> = emptyList()

    suspend fun catalog(): List<Item> {
        if (cache.isEmpty()) cache = db.itemDao().getAll()
        return cache
    }

    suspend fun refresh() {
        cache = db.itemDao().getAll()
    }

    suspend fun fuzzyMatch(input: String): Item? {
        val catalog = catalog()
        val normalized = input.lowercase().trim()
        if (normalized.isBlank()) return null

        // 1. Exact match on any name field
        catalog.find {
            it.nameHindi.lowercase() == normalized ||
                it.nameKannada.lowercase() == normalized ||
                it.nameEnglish.lowercase() == normalized
        }?.let { return it }

        // 2. Alias map (common speech, Hinglish, brand shorthand)
        ALIASES[normalized]?.let { alias ->
            catalog.find { it.nameEnglish.equals(alias, ignoreCase = true) }?.let { return it }
        }

        // 3. Word-level containment (either direction)
        catalog.find { item ->
            val en = item.nameEnglish.lowercase()
            val hi = item.nameHindi.lowercase()
            normalized.contains(en) || en.contains(normalized) ||
                normalized.contains(hi) || hi.contains(normalized) ||
                en.split(" ").any { w -> w.length > 3 && normalized.contains(w) }
        }?.let { return it }

        // 4. Alias containment ("ek packet parle de do" contains "parle")
        for ((alias, target) in ALIASES) {
            if (normalized.contains(alias)) {
                catalog.find { it.nameEnglish.equals(target, ignoreCase = true) }?.let { return it }
            }
        }

        // 5. Levenshtein fallback for mishears/typos
        val best = catalog.minByOrNull { minDistance(normalized, it) }
        return best?.takeIf { minDistance(normalized, it) <= 3 }
    }

    private fun minDistance(input: String, item: Item): Int =
        minOf(
            levenshtein(input, item.nameEnglish.lowercase()),
            levenshtein(input, item.nameHindi.lowercase()),
            item.nameEnglish.lowercase().split(" ").minOfOrNull { levenshtein(input, it) } ?: Int.MAX_VALUE
        )

    companion object {
        val ALIASES = mapOf(
            "parle" to "Parle-G", "parle g" to "Parle-G", "gluco" to "Parle-G", "biscuit" to "Parle-G",
            "maggi" to "Maggi Noodles", "noodles" to "Maggi Noodles", "maggie" to "Maggi Noodles",
            "cheeni" to "Cheeni", "sugar" to "Cheeni", "shakkar" to "Cheeni", "sakkare" to "Cheeni",
            "namak" to "Salt", "uppu" to "Salt",
            "tel" to "Sunflower Oil", "oil" to "Sunflower Oil", "enne" to "Sunflower Oil",
            "saffola" to "Saffola Oil",
            "atta" to "Aashirvaad Atta", "aata" to "Aashirvaad Atta", "wheat flour" to "Aashirvaad Atta",
            "hittu" to "Aashirvaad Atta",
            "maida" to "Maida",
            "doodh" to "Nandini Milk", "milk" to "Nandini Milk", "haalu" to "Nandini Milk",
            "nandini" to "Nandini Milk",
            "chai" to "Tata Tea", "tea" to "Tata Tea", "chaha" to "Tata Tea",
            "coffee" to "Nescafe Coffee", "nescafe" to "Nescafe Coffee",
            "sabun" to "Lifebuoy", "soap" to "Lifebuoy", "soapu" to "Lifebuoy",
            "toothpaste" to "Colgate", "colgate" to "Colgate", "paste" to "Colgate",
            "dal" to "Toor Dal", "daal" to "Toor Dal", "toor" to "Toor Dal", "bele" to "Toor Dal",
            "moong" to "Moong Dal", "chana" to "Chana Dal",
            "chawal" to "Sona Masoori Rice", "rice" to "Sona Masoori Rice", "akki" to "Sona Masoori Rice",
            "basmati" to "Basmati Rice",
            "anda" to "Eggs", "ande" to "Eggs", "egg" to "Eggs", "motte" to "Eggs",
            "chocolate" to "Dairy Milk", "dairy milk" to "Dairy Milk",
            "surf" to "Surf Excel", "detergent" to "Surf Excel",
            "pyaz" to "Onion", "kanda" to "Onion", "onion" to "Onion", "eerulli" to "Onion",
            "aloo" to "Potato", "batata" to "Potato", "potato" to "Potato",
            "tamatar" to "Tomato", "tomato" to "Tomato",
            "gud" to "Jaggery", "bella" to "Jaggery", "jaggery" to "Jaggery",
            "haldi" to "Haldi Powder", "mirchi" to "Chilli Powder", "dhania" to "Dhania Powder",
            "besan" to "Besan", "ghee" to "Ghee", "tuppa" to "Ghee",
            "dahi" to "Curd", "curd" to "Curd", "mosaru" to "Curd",
            "paneer" to "Paneer", "bread" to "Bread", "poha" to "Poha", "rava" to "Sooji Rava",
            "sooji" to "Sooji Rava", "suji" to "Sooji Rava",
            "chips" to "Lays Chips", "lays" to "Lays Chips", "kurkure" to "Kurkure",
            "horlicks" to "Horlicks", "bournvita" to "Bournvita",
            "vim" to "Vim Bar", "rin" to "Rin Soap", "dettol" to "Dettol",
            "lifebuoy" to "Lifebuoy", "hajmola" to "Hajmola",
            "agarbatti" to "Agarbatti", "jeera" to "Jeera", "garam masala" to "Garam Masala",
            "kitkat" to "KitKat", "kit kat" to "KitKat"
        )

        fun levenshtein(a: String, b: String): Int {
            if (a == b) return 0
            if (a.isEmpty()) return b.length
            if (b.isEmpty()) return a.length
            val prev = IntArray(b.length + 1) { it }
            val cur = IntArray(b.length + 1)
            for (i in 1..a.length) {
                cur[0] = i
                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    cur[j] = minOf(cur[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
                }
                cur.copyInto(prev)
            }
            return prev[b.length]
        }
    }
}
