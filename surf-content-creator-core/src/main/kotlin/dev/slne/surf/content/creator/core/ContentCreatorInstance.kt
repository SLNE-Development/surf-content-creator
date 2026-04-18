package dev.slne.surf.content.creator.core

import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.content.creator.api.ContentCreatorPlatform
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import dev.slne.surf.content.creator.api.platform.PlatformState

object ContentCreatorInstance {

    private val stateChangeListeners = mutableObjectSetOf<StateChangeListener>()

    fun registerStateChangeListener(listener: StateChangeListener) {
        stateChangeListeners.add(listener)
    }

    fun callOnStateChangeListener(
        contentCreatorPlatform: ContentCreatorPlatform,
        newState: PlatformState
    ) {
        stateChangeListeners.forEach { it.onStateChanged(contentCreatorPlatform, newState) }
    }

}