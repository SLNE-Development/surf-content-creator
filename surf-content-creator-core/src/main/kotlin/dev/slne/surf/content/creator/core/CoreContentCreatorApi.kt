package dev.slne.surf.content.creator.core

import dev.slne.surf.content.creator.api.ContentCreatorApi
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import net.kyori.adventure.text.Component
import java.nio.file.Path
import java.util.*

abstract class CoreContentCreatorApi : ContentCreatorApi {
    abstract val dataPath: Path

    override fun registerStateChangeListener(listener: StateChangeListener) {
        ContentCreatorInstance.registerStateChangeListener(listener)
    }

    override fun renderLiveTag(playerUuid: UUID, space: Boolean): Component =
        LiveTagRenderer.render(playerUuid, space)
}

val coreApi get() = ContentCreatorApi.INSTANCE as CoreContentCreatorApi