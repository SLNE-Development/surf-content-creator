package dev.slne.surf.content.creator.api

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import net.kyori.adventure.text.Component
import java.util.*

private val api = requiredService<ContentCreatorApi>()

interface ContentCreatorApi {

    /**
     * Registers a new [StateChangeListener] to listen for state changes.
     *
     * @param listener listener
     */
    fun registerStateChangeListener(listener: StateChangeListener)
    fun renderLiveTag(playerUuid: UUID, space: Boolean = true): Component

    companion object : ContentCreatorApi by api {
        val INSTANCE get() = api
    }
}