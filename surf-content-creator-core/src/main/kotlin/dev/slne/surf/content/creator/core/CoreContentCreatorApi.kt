package dev.slne.surf.content.creator.core

import dev.slne.surf.content.creator.api.ContentCreatorApi
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import java.nio.file.Path

abstract class CoreContentCreatorApi : ContentCreatorApi {
    abstract val dataPath: Path

    override fun registerStateChangeListener(listener: StateChangeListener) {
        ContentCreatorInstance.registerStateChangeListener(listener)
    }
}

val coreApi get() = ContentCreatorApi.INSTANCE as CoreContentCreatorApi