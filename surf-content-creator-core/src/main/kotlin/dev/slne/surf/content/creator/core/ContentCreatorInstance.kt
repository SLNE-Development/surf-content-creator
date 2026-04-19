package dev.slne.surf.content.creator.core

import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.content.creator.api.ContentCreatorPlatform
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import dev.slne.surf.content.creator.api.platform.PlatformState
import java.util.*

object ContentCreatorInstance {

    private val stateChangeListeners = mutableObjectSetOf<StateChangeListener>()

    fun registerStateChangeListener(listener: StateChangeListener) {
        stateChangeListeners.add(listener)
    }

    fun callOnStateChangeListener(
        playerUuid: UUID,
        contentCreatorPlatform: ContentCreatorPlatform,
        newState: PlatformState
    ) {
        stateChangeListeners.forEach { it.onStateChanged(playerUuid, contentCreatorPlatform, newState) }
    }
}