package dev.slne.surf.content.creator.api

import dev.slne.surf.content.creator.api.listener.StateChangeListener
import dev.slne.surf.surfapi.core.api.util.requiredService

val api get() = ContentCreatorApi.INSTANCE

interface ContentCreatorApi {

    /**
     * Registers a new [StateChangeListener] to listen for state changes.
     *
     * @param listener listener
     */
    fun registerStateChangeListener(listener: StateChangeListener)

    companion object {
        val INSTANCE = requiredService<ContentCreatorApi>()
    }

}