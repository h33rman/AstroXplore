package com.example.astroxplore.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AdsTexSanitizerTest {

    @Test
    fun testIonParsing() {
        val input = "Emission in \\ion{Fe}{II} and \\ion{H}{II} regions"
        val expected = "Emission in Fe II and H II regions"
        assertEquals(expected, AdsTexSanitizer.sanitize(input))
    }

    @Test
    fun testSpecialSymbols() {
        val input = "Mass of \$M_{\\odot}\$ and \$L_{\\odot}\$"
        val expected = "Mass of M☉ and L☉"
        assertEquals(expected, AdsTexSanitizer.sanitize(input))
    }

    @Test
    fun testSuperscript() {
        val input = "Density is \$10^{14}\$ cm\$^{-3}\$"
        // Note: our plain text sanitizer might not be perfect for nested complex math, but let's test basic.
        val result = AdsTexSanitizer.sanitize(input)
        assert(result.contains("10¹⁴"))
    }

    @Test
    fun testFormattingRemoval() {
        val input = "A \\textit{very} \\textbf{important} result"
        val expected = "A very important result"
        assertEquals(expected, AdsTexSanitizer.sanitize(input))
    }

    @Test
    fun testHtmlTagRemoval() {
        val input = "Title <mml:math>with math</mml:math> tags"
        val expected = "Title with math tags"
        assertEquals(expected, AdsTexSanitizer.sanitize(input))
    }
}
