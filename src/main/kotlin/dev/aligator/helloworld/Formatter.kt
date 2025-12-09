package dev.aligator.helloworld

import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.net.URI
private val FORMAT_PATTERN = Regex("""(?i)(§[0-9A-FK-OR])""")
private val URL_PATTERN = Regex("""\[([^\n]*?)]\((.+)\)""")

class Formatter {
    private fun matchURLs(input: String): List<String> {
        val parts = mutableListOf<String>()
        var lastIndex = 0

        for (match in URL_PATTERN.findAll(input)) {
            val start = match.range.first
            val end = match.range.last + 1

            // Capture text before the link if there's any
            if (start > lastIndex) {
                parts.add(input.substring(lastIndex, start))
            }

            // Capture the link itself
            parts.add(match.value)

            // Update lastIndex to just after the current match
            lastIndex = end
        }

        // Capture any remaining text after the last link
        if (lastIndex < input.length) {
            parts.add(input.substring(lastIndex))
        }

        return parts
    }

    fun format(input: String): Text {
        val splitOnLinks = matchURLs(input)

        var lastColor: Formatting? = null
        var lastModifier: Formatting? = null
        var final = Text.literal("")

        splitOnLinks.forEach { value ->
            // Get the text to display.
            val matchedUrl = URL_PATTERN.matchEntire(value)
            var originalText = value
            if (matchedUrl != null) {
                // Or get the url text.
                originalText = matchedUrl.groups[1]?.value ?: value
            }

            // Apply the currently active formatting.
            var newText = originalText
            val modifier = lastModifier
            if (modifier != null) {
                newText = "§${modifier.code}$newText"
            }
            val color = lastColor
            if (color != null) {
                newText = "§${color.code}$newText"
            }

            // Find the last used formats from the original text
            // to preserve them for the next text.
            val match = FORMAT_PATTERN.findAll(originalText)
            match.map { matchedValue -> Formatting.byCode(matchedValue.value[1]) }
                .forEach { formatting ->
                    if (formatting?.isModifier == true) {
                        lastModifier = formatting
                    } else if (formatting?.isColor == true) {
                        lastColor = formatting
                    }
                }

            // Render the URL or the text.
            if (matchedUrl != null) {
                val url = matchedUrl.groups[2]?.value
                var urlLiteral = Text.literal(newText)
                if (url != null) {
                    val clickEvent = ClickEvent.OpenUrl(URI.create(url))
                    urlLiteral = urlLiteral.fillStyle(urlLiteral.style.withClickEvent(clickEvent))
                }

                final.append(urlLiteral)
            } else {
                final.append(Text.literal(newText))
            }
        }
        return final
    }
}
