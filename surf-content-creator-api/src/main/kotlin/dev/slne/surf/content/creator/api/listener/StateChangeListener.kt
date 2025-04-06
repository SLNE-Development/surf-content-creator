package dev.slne.surf.content.creator.api.listener

import dev.slne.surf.content.creator.api.ContentCreatorPlatform
import dev.slne.surf.content.creator.api.platform.PlatformState

interface StateChangeListener {

    /**
     * Called when the state of a [ContentCreatorPlatform] has changed.
     *
     * @param contentCreatorPlatform the content creator plattform
     * @param newState the new state
     */
    fun onStateChanged(contentCreatorPlatform: ContentCreatorPlatform, newState: PlatformState)

}