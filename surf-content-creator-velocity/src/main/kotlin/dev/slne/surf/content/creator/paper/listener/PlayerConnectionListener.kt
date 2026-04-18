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
        plugin.launch {
            ContentCreatorService.cacheCreator(event.player.uniqueId)
            ContentClientManager.enableStreamEventListener(event.player.uniqueId)
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        ContentCreatorService.invalidate(event.player.uniqueId)

        plugin.launch {
            ContentClientManager.disableStreamEventListener(event.player.uniqueId)
        }
    }
}