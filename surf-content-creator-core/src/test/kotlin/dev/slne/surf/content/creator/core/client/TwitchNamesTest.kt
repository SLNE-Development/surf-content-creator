package dev.slne.surf.content.creator.core.client

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TwitchNamesTest {

    @Test
    fun `accepts names within the allowed length`() {
        assertTrue(isValidTwitchName("twisti"))
        assertTrue(isValidTwitchName("abcd"))
        assertTrue(isValidTwitchName("a".repeat(25)))
        assertTrue(isValidTwitchName("Some_Name_123"))
    }

    @Test
    fun `rejects names that are too short or too long`() {
        assertFalse(isValidTwitchName("abc"))
        assertFalse(isValidTwitchName("a".repeat(26)))
        assertFalse(isValidTwitchName(""))
    }

    @Test
    fun `rejects names with unsupported characters`() {
        assertFalse(isValidTwitchName("some name"))
        assertFalse(isValidTwitchName("some-name"))
        assertFalse(isValidTwitchName("näme"))
        assertFalse(isValidTwitchName("name\n"))
    }

    @Test
    fun `partitions names into valid and invalid ones`() {
        val partition = partitionTwitchNames(listOf("twisti", "no", "red_", "x"))

        assertEquals(listOf("twisti", "red_"), partition.valid)
        assertEquals(listOf("no", "x"), partition.invalid)
    }

    @Test
    fun `partitions empty input into empty results`() {
        val partition = partitionTwitchNames(emptyList())

        assertTrue(partition.valid.isEmpty())
        assertTrue(partition.invalid.isEmpty())
    }
}
