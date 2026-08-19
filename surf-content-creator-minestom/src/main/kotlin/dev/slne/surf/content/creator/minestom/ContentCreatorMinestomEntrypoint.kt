package dev.slne.surf.content.creator.minestom

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.plugin.MinestomPluginEntrypoint
import dev.slne.minestom.lobby.api.plugin.annotation.DataDirectory
import dev.slne.surf.content.creator.core.ContentCreatorLifecycle
import dev.slne.surf.content.creator.core.config.loadContentCreatorConfig
import java.nio.file.Path

@Singleton
class ContentCreatorMinestomEntrypoint @Inject constructor(
    @DataDirectory path: Path
) : MinestomPluginEntrypoint {

    init {
        dataPath = path
    }

    override suspend fun start() {
        loadContentCreatorConfig()

        ContentCreatorLifecycle.enable()
    }

    override suspend fun stop() {
        ContentCreatorLifecycle.disable()
    }

    companion object {
        lateinit var dataPath: Path
    }
}
