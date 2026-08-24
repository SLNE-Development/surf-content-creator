package dev.slne.surf.content.creator.paper.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.content.creator.core.client.ContentClientManager
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import dev.slne.surf.content.creator.paper.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object PlayerConnectionListener : Listener {
    @EventHandler
    fun onConnect(event: PlayerJoinEvent) {
        val playerUuid = event.player.uniqueId

        ContentCreatorService.beginSession(playerUuid)

        plugin.launch {
            ContentCreatorService.cacheCreator(playerUuid)
            ContentClientManager.enableStreamEventListener(playerUuid)
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        val creator = ContentCreatorService.endSession(event.player.uniqueId) ?: return

        plugin.launch {
            ContentClientManager.disableStreamEventListener(creator)
        }
    }
}
