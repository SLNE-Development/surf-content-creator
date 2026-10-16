package dev.slne.surf.content.creator.minestom

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.content.creator.core.ContentCreatorLifecycle
import dev.slne.surf.content.creator.core.config.loadContentCreatorConfig
import dev.slne.surf.content.creator.minestom.command.contentCreatorCommand
import dev.slne.surf.content.creator.minestom.listener.PlayerConnectionListener

object ContentCreatorMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        loadContentCreatorConfig()

        ContentCreatorLifecycle.enable()
    }

    override suspend fun onEnable() {
        contentCreatorCommand()

        PlayerConnectionListener.register(eventNode)
    }

    override suspend fun onDisable() {
        ContentCreatorLifecycle.disable()
    }
}
