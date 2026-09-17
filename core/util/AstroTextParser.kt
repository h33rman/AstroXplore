package com.example.astroxplore.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

object AstroTextParser {

    /**
     * Parses academic text containing HTML tags (<SUB>, <SUP>, <B>, <I>) into a Compose AnnotatedString.
     * Also handles TeX symbols and basic LaTeX formatting.
     */
    fun parse(input: String): AnnotatedString {
        if (input.isBlank()) return AnnotatedString("")

        // 1. Initial cleanup of complex MathML or structural XML that we don't handle natively
        var text = input
            .replace(Regex("<mml:math.*?>.*?</mml:math>", RegexOption.IGNORE_CASE)) { 
                it.value.replace(Regex("<[^>]*>"), "") // Strip tags within math blocks
            }
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("~", " ")

        return buildAnnotatedString {
            var currentIndex = 0
            
            // Matches HTML-style tags and simple TeX macros
            val tagRegex = Regex(
                """<(SUB|SUP|B|I|STRONG|EM)>(.*?)</\1>|""" + // Group 1 (Tag), 2 (Content)
                """\\ion\{([^}]*)\}\{([^}]*)\}|""" +          // Group 3, 4
                """\\textit\{([^}]*)\}|""" +                  // Group 5
                """\\textbf\{([^}]*)\}|""" +                  // Group 6
                """\^\{([^}]*)\}|""" +                        // Group 7
                """_\{([^}]*)\}|""" +                         // Group 8
                """\$([^$]*)\$"""                             // Group 9
            , RegexOption.IGNORE_CASE)
            
            val matches = tagRegex.findAll(text)
            
            for (match in matches) {
                // Pre-match text
                append(text.substring(currentIndex, match.range.first))
                
                when {
                    match.groups[1] != null -> { // HTML Tags
                        val tag = match.groupValues[1].uppercase()
                        val content = match.groupValues[2]
                        applyStyle(tag, content)
                    }
                    match.groups[3] != null -> { // \ion{H}{II}
                        append("${match.groupValues[3]} ${match.groupValues[4]}")
                    }
                    match.groups[5] != null -> { // \textit
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(match.groupValues[5]) }
                    }
                    match.groups[6] != null -> { // \textbf
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(match.groupValues[6]) }
                    }
                    match.groups[7] != null -> { // ^{...}
                        withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 11.sp)) { append(match.groupValues[7]) }
                    }
                    match.groups[8] != null -> { // _{...}
                        withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 11.sp)) { append(match.groupValues[8]) }
                    }
                    match.groups[9] != null -> { // $...$
                        append(AdsTexSanitizer.sanitize("$" + match.groupValues[9] + "$"))
                    }
                }
                currentIndex = match.range.last + 1
            }
            
            if (currentIndex < text.length) {
                append(text.substring(currentIndex).replace(Regex("<[^>]*>"), ""))
            }
        }
    }

    private fun AnnotatedString.Builder.applyStyle(tag: String, content: String) {
        when (tag) {
            "SUB" -> withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 11.sp)) { append(content) }
            "SUP" -> withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 11.sp)) { append(content) }
            "B", "STRONG" -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(content) }
            "I", "EM" -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(content) }
            else -> append(content)
        }
    }
}
