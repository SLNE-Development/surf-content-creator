package dev.slne.surf.content.creator.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.plugin.MinestomPlugin
import dev.slne.minestom.lobby.api.plugin.annotation.MinestomPluginMeta
import dev.slne.surf.content.creator.minestom.command.ContentCreatorCommands
import dev.slne.surf.content.creator.minestom.listener.PlayerConnectionListener

@AutoService(MinestomPlugin::class)
@MinestomPluginMeta(
    "surf-content-creator-minestom",
    dependsOn = ["surf-api-minestom", "surf-social-minestom"]
)
class ContentCreatorMinestomPlugin :
    MinestomPlugin(ContentCreatorMinestomEntrypoint::class.java) {

    override fun configurePlugin() {
        bindEventRegistrar<PlayerConnectionListener>()
        bindCommandRegistrar<ContentCreatorCommands>()
    }
}
