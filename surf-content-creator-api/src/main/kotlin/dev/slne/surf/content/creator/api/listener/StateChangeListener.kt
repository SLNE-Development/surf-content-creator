package dev.slne.surf.content.creator.api.listener

import dev.slne.surf.content.creator.api.ContentCreatorPlatform
import dev.slne.surf.content.creator.api.platform.PlatformState
import java.util.UUID

interface StateChangeListener {

    /**
     * Called when the state of a [ContentCreatorPlatform] has changed.
     *
     * @param playerUuid the minecraft uuid of the affected content creator
     * @param contentCreatorPlatform the content creator plattform
     * @param newState the new state
     */
    fun onStateChanged(playerUuid: UUID, contentCreatorPlatform: ContentCreatorPlatform, newState: PlatformState)

}