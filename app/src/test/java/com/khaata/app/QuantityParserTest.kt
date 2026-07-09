package com.khaata.app

import com.khaata.app.agent.QuantityParser
import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityParserTest {

    @Test fun `hindi number words`() {
        assertEquals(2.0, QuantityParser.parse("do kilo cheeni"), 0.0)
        assertEquals(3.0, QuantityParser.parse("teen maggi"), 0.0)
        assertEquals(5.0, QuantityParser.parse("paanch parle"), 0.0)
        assertEquals(0.5, QuantityParser.parse("adha kilo namak"), 0.0)
        assertEquals(0.25, QuantityParser.parse("paav kilo dal"), 0.0)
        assertEquals(1.5, QuantityParser.parse("dedh litre doodh"), 0.0)
    }

    @Test fun `kannada number words`() {
        assertEquals(2.0, QuantityParser.parse("eradu kg akki"), 0.0)
        assertEquals(1.0, QuantityParser.parse("ondu motte"), 0.0)
    }

    @Test fun `digits win over words`() {
        assertEquals(3.0, QuantityParser.parse("3 packet maggi"), 0.0)
        assertEquals(2.5, QuantityParser.parse("2.5 kg atta"), 0.0)
    }

    @Test fun `defaults to one`() {
        assertEquals(1.0, QuantityParser.parse("cheeni"), 0.0)
    }

    @Test fun `strip quantity leaves item name`() {
        assertEquals("cheeni", QuantityParser.stripQuantity("do kilo cheeni"))
        assertEquals("maggi", QuantityParser.stripQuantity("3 packet maggi"))
        assertEquals("parle-g", QuantityParser.stripQuantity("ek Parle-G"))
    }
}
