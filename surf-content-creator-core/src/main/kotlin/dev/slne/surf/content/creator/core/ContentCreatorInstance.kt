package dev.slne.surf.content.creator.core

import dev.slne.surf.content.creator.api.ContentCreatorPlattform
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import dev.slne.surf.content.creator.api.plattform.PlattformState
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf

object ContentCreatorInstance {

    private val stateChangeListeners = mutableObjectSetOf<StateChangeListener>()

    fun registerStateChangeListener(listener: StateChangeListener) {
        stateChangeListeners.add(listener)
    }

    fun callOnStateChangeListener(
        contentCreatorPlattform: ContentCreatorPlattform,
        newState: PlattformState
    ) {
        stateChangeListeners.forEach { it.onStateChanged(contentCreatorPlattform, newState) }
    }

}