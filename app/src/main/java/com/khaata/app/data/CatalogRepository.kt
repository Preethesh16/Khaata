package com.khaata.app.data

import java.util.Locale

/**
 * Catalog lookups with fuzzy matching:
 * exact -> contains -> alias map -> Levenshtein fallback.
 * Keeps an in-memory snapshot of the catalog for fast matching;
 * stock reads/writes always go through Room.
 */
class CatalogRepository(private val db: AppDatabase) {

    @Volatile private var catalog: List<Item> = emptyList()

    suspend fun refresh() {
        catalog = db.itemDao().getAll()
    }

    suspend fun getById(id: Int): Item? = db.itemDao().getById(id)

    suspend fun fuzzyMatch(input: String): Item? {
        if (catalog.isEmpty()) refresh()
        return match(catalog, input)
    }

    companion object {

        /** Pure matching core — also exercised directly by JVM unit tests. */
        fun match(catalog: List<Item>, input: String): Item? {
        val normalized = input.lowercase(Locale.ROOT).trim()
        if (normalized.isBlank()) return null

        // 1. Exact match on any name field
        catalog.find {
            it.nameHindi.lowercase(Locale.ROOT) == normalized ||
            it.nameKannada.lowercase(Locale.ROOT) == normalized ||
            it.nameEnglish.lowercase(Locale.ROOT) == normalized
        }?.let { return it }

        // 2. Contains match (both directions), English + Hindi + Kannada
        catalog.find {
            val en = it.nameEnglish.lowercase(Locale.ROOT)
            val hi = it.nameHindi.lowercase(Locale.ROOT)
            val kn = it.nameKannada.lowercase(Locale.ROOT)
            normalized.contains(en) || en.contains(normalized) ||
            normalized.contains(hi) || hi.contains(normalized) ||
            normalized.contains(kn) || kn.contains(normalized)
        }?.let { return it }

        // 3. Alias map — common spoken names and mishears
        ALIASES[normalized]?.let { aliasTarget ->
            catalog.find { it.nameEnglish.equals(aliasTarget, ignoreCase = true) }
                ?.let { return it }
        }
        // Alias may also appear inside a longer utterance chunk
        ALIASES.entries.find { normalized.contains(it.key) }?.let { entry ->
            catalog.find { it.nameEnglish.equals(entry.value, ignoreCase = true) }
                ?.let { return it }
        }

        // 4. Levenshtein distance fallback on English names (typos / STT mishears)
        return catalog
            .map { it to levenshtein(normalized, it.nameEnglish.lowercase(Locale.ROOT)) }
            .minByOrNull { it.second }
            ?.takeIf { it.second <= 3 }
            ?.first
        }

        val ALIASES = mapOf(
            "parle" to "Parle-G", "parle g" to "Parle-G", "gluco" to "Parle-G", "biscuit" to "Parle-G",
            "maggi" to "Maggi Noodles", "noodles" to "Maggi Noodles", "maggie" to "Maggi Noodles",
            "cheeni" to "Cheeni", "sugar" to "Cheeni", "shakkar" to "Cheeni", "sakkare" to "Cheeni",
            "namak" to "Salt", "uppu" to "Salt",
            "tel" to "Sunflower Oil", "oil" to "Sunflower Oil", "enne" to "Sunflower Oil",
            "atta" to "Aashirvaad Atta", "wheat flour" to "Aashirvaad Atta", "aata" to "Aashirvaad Atta",
            "maida" to "Maida",
            "doodh" to "Milk", "haalu" to "Milk", "milk" to "Milk",
            "chai" to "Tata Tea", "tea" to "Tata Tea", "chaha" to "Tata Tea",
            "coffee" to "Nescafe Coffee",
            "sabun" to "Lifebuoy", "soap" to "Lifebuoy", "sopu" to "Lifebuoy",
            "toothpaste" to "Colgate", "paste" to "Colgate",
            "chawal" to "Sona Masoori Rice", "rice" to "Sona Masoori Rice", "akki" to "Sona Masoori Rice",
            "dal" to "Toor Dal", "toor" to "Toor Dal", "arhar" to "Toor Dal", "bele" to "Toor Dal",
            "moong" to "Moong Dal", "chana" to "Chana Dal",
            "anda" to "Eggs", "ande" to "Eggs", "egg" to "Eggs", "motte" to "Eggs",
            "dahi" to "Curd", "mosaru" to "Curd", "curd" to "Curd",
            "ghee" to "Ghee", "tuppa" to "Ghee",
            "chocolate" to "Dairy Milk", "cadbury" to "Dairy Milk",
            "pyaz" to "Onion", "kanda" to "Onion", "eerulli" to "Onion", "onion" to "Onion",
            "aloo" to "Potato", "batata" to "Potato", "potato" to "Potato",
            "tamatar" to "Tomato", "tomato" to "Tomato",
            "haldi" to "Haldi Powder", "mirchi" to "Mirchi Powder",
            "surf" to "Surf Excel", "detergent" to "Surf Excel",
            "pani" to "Bisleri Water", "water" to "Bisleri Water",
            "chips" to "Lays Chips",
            "bread" to "Bread", "double roti" to "Bread",
            "matchbox" to "Matchbox", "machis" to "Matchbox"
        )

        fun levenshtein(a: String, b: String): Int {
            if (a == b) return 0
            if (a.isEmpty()) return b.length
            if (b.isEmpty()) return a.length
            val prev = IntArray(b.length + 1) { it }
            val curr = IntArray(b.length + 1)
            for (i in 1..a.length) {
                curr[0] = i
                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    curr[j] = minOf(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
                }
                prev.indices.forEach { prev[it] = curr[it] }
            }
            return curr[b.length]
        }
    }
}
