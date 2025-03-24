package dev.slne.surf.content.creator.api.listener

import dev.slne.surf.content.creator.api.ContentCreatorPlattform
import dev.slne.surf.content.creator.api.plattform.PlattformState

interface StateChangeListener {

    /**
     * Called when the state of a [ContentCreatorPlattform] has changed.
     *
     * @param contentCreatorPlattform the content creator plattform
     * @param newState the new state
     */
    fun onStateChanged(contentCreatorPlattform: ContentCreatorPlattform, newState: PlattformState)

}