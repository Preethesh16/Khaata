package com.khaata.app

import com.khaata.app.data.CatalogRepository
import com.khaata.app.data.CatalogSeeder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogMatchTest {

    private val catalog = CatalogSeeder.CATALOG
    private fun match(input: String) = CatalogRepository.match(catalog, input)?.nameEnglish

    @Test fun `exact english name`() {
        assertEquals("Maggi Noodles", match("Maggi Noodles"))
    }

    @Test fun `alias hits from the master plan`() {
        assertEquals("Parle-G", match("Parle"))
        assertEquals("Parle-G", match("gluco"))
        assertEquals("Cheeni", match("cheeni"))
        assertEquals("Cheeni", match("sugar"))
        assertEquals("Salt", match("namak"))
        assertEquals("Sunflower Oil", match("tel"))
        assertEquals("Milk", match("doodh"))
        assertEquals("Maggi Noodles", match("maggi"))
    }

    @Test fun `contains match on partial names`() {
        assertEquals("Dairy Milk", match("dairy milk chocolate"))
        assertEquals("Tata Tea", match("tata"))
    }

    @Test fun `levenshtein rescues typos and mishears`() {
        assertEquals("Colgate", match("colgat"))
        assertEquals("Bread", match("bred"))
    }

    @Test fun `hindi and kannada names resolve`() {
        assertEquals("Cheeni", match("चीनी"))
        assertEquals("Sona Masoori Rice", match("akki"))
    }

    @Test fun `garbage returns null`() {
        assertNull(match("xylophone quantum"))
        assertNull(match(""))
    }

    @Test fun `catalog has 50 items with unique ids`() {
        assertEquals(50, catalog.size)
        assertEquals(50, catalog.map { it.id }.toSet().size)
    }

    @Test fun `all alias targets exist in catalog`() {
        val names = catalog.map { it.nameEnglish }.toSet()
        CatalogRepository.ALIASES.values.toSet().forEach { target ->
            assert(target in names) { "alias target '$target' missing from catalog" }
        }
    }
}
