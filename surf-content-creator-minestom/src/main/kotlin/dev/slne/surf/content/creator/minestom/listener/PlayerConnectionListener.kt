package dev.slne.surf.content.creator.minestom.listener

import dev.slne.minestom.lobby.api.coroutine.minestomAsyncScope
import dev.slne.minestom.lobby.api.event.EventRegistrar
import dev.slne.minestom.lobby.api.extension.addListener
import dev.slne.surf.content.creator.core.client.ContentClientManager
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import kotlinx.coroutines.launch
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent

class PlayerConnectionListener : EventRegistrar {

    override fun register(node: EventNode<Event>) {
        node.addListener<PlayerSpawnEvent> { event ->
            if (!event.isFirstSpawn) {
                return@addListener
            }

            val playerUuid = event.player.uuid

            ContentCreatorService.beginSession(playerUuid)

            minestomAsyncScope.launch {
                ContentCreatorService.cacheCreator(playerUuid)
                ContentClientManager.enableStreamEventListener(playerUuid)
            }
        }

        node.addListener<PlayerDisconnectEvent> { event ->
            val creator = ContentCreatorService.endSession(event.player.uuid) ?: return@addListener

            minestomAsyncScope.launch {
                ContentClientManager.disableStreamEventListener(creator)
            }
        }
    }
}
