package com.retrobot.core.util

import com.retrobot.core.Discord.Markdown.OP_BOLD
import com.retrobot.core.Discord.Markdown.OP_CODE_BLOCK
import com.retrobot.core.Discord.Markdown.OP_CODE_LINE
import com.retrobot.core.Discord.Markdown.OP_ITALIC
import com.retrobot.core.Discord.Markdown.OP_ITALIC_ALT
import com.retrobot.core.Discord.Markdown.OP_QUOTE_BLOCK
import com.retrobot.core.Discord.Markdown.OP_QUOTE_LINE
import com.retrobot.core.Discord.Markdown.OP_SPOILER
import com.retrobot.core.Discord.Markdown.OP_STRIKE
import com.retrobot.core.Discord.Markdown.OP_UNDERLINE

/**
 * Utility functions for generating Discord Markdown text.
 */
object Markdown {

    /**
     * Sanitize will escape all Markdown characters in the given [text].
     * This will stop unnecessary formatting from happening in Discord.
     *
     * @param text The [String] to be sanitized
     * @return The sanitized [String]
     */
    fun sanitize(text: String) : String {
        return text.replace(OP_CODE_BLOCK, "\\$OP_CODE_BLOCK")
                .replace(OP_CODE_LINE, "\\$OP_CODE_LINE")
                .replace(OP_BOLD, "\\$OP_BOLD")
                .replace(OP_ITALIC, "\\$OP_ITALIC")
                .replace(OP_ITALIC_ALT, "\\$OP_ITALIC_ALT")
                .replace(OP_SPOILER, "\\$OP_SPOILER")
                .replace(OP_STRIKE, "\\$OP_STRIKE")
                .replace(OP_QUOTE_BLOCK, "\\$OP_QUOTE_BLOCK")
                .replace(OP_QUOTE_LINE, "\\$OP_QUOTE_LINE")
                .replace(OP_UNDERLINE, "\\$OP_UNDERLINE")
    }

    fun codeBlock(text: String) : String {
        return "$OP_CODE_BLOCK$text$OP_CODE_BLOCK"
    }

    fun codeLine(text: String) : String {
        return "$OP_CODE_LINE$text$OP_CODE_LINE"
    }

    fun embolden(text: String) : String {
        return "$OP_BOLD$text$OP_BOLD"
    }

    fun hyperlink(name: String, url: String) : String {
        return "[$name]($url)"
    }

    fun italicize(text: String) : String {
        return "$OP_ITALIC$text$OP_ITALIC"
    }

    fun spoiler(text: String) : String {
        return "$OP_SPOILER$text$OP_SPOILER"
    }

    fun strikethrough(text: String) : String {
        return "$OP_STRIKE$text$OP_STRIKE"
    }

    fun quoteBlock(text: String) : String {
        return "$OP_QUOTE_BLOCK$text"
    }

    fun quoteLine(text: String) : String {
        return "$OP_QUOTE_LINE$text"
    }

    fun underline(text: String) : String {
        return "$OP_UNDERLINE$text$OP_UNDERLINE"
    }
}