package dev.slne.surf.content.creator.core

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LiveTagRendererTest {

    private val liveTag = Component.text("●")

    @Test
    fun `renders nothing for an offline creator`() {
        assertEquals("", plain(live = false, space = true))
        assertEquals("", plain(live = false, space = false))
    }

    @Test
    fun `renders the tag for a live creator`() {
        assertEquals("●", plain(live = true, space = false))
    }

    @Test
    fun `prefixes the tag with a space when requested`() {
        assertEquals(" ●", plain(live = true, space = true))
    }

    private fun plain(live: Boolean, space: Boolean) = PlainTextComponentSerializer.plainText()
        .serialize(LiveTagRenderer.renderTag(live, space, liveTag))
}
