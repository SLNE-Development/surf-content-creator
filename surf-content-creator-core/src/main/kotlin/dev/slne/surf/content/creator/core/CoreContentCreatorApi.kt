package dev.slne.surf.content.creator.core

import com.google.auto.service.AutoService
import dev.slne.surf.content.creator.api.ContentCreatorApi
import dev.slne.surf.content.creator.api.api
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import net.kyori.adventure.util.Services.Fallback
import java.nio.file.Path

abstract class CoreContentCreatorApi : ContentCreatorApi {

    abstract val dataPath: Path

    override fun registerStateChangeListener(listener: StateChangeListener) {
        ContentCreatorInstance.registerStateChangeListener(listener)
    }
}

val coreApi get() = api as CoreContentCreatorApi