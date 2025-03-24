package dev.slne.surf.content.creator.core

import com.google.auto.service.AutoService
import dev.slne.surf.content.creator.api.ContentCreatorApi
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import net.kyori.adventure.util.Services.Fallback

@AutoService(ContentCreatorApi::class)
class CoreContentCreatorApi : ContentCreatorApi, Fallback {

    override fun registerStateChangeListener(listener: StateChangeListener) {
        ContentCreatorInstance.registerStateChangeListener(listener)
    }
}