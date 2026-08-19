package dev.slne.surf.content.creator.core

import dev.slne.surf.content.creator.core.client.ContentClientManager
import dev.slne.surf.content.creator.core.platform.platformBridge

/**
 * Starts and stops the platform-neutral part of the content creator plugin.
 */
object ContentCreatorLifecycle {

    /**
     * Connects every content client and starts listening for the streams of all online players.
     */
    suspend fun enable() {
        ContentClientManager.startAll(platformBridge.pluginScope)
        ContentClientManager.enableStreamEventListener(platformBridge.onlinePlayerUuids())
    }

    /**
     * Closes every content client.
     */
    fun disable() {
        ContentClientManager.closeAll()
    }
}
