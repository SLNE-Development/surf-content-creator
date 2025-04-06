package dev.slne.surf.content.creator.core.client

import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.core.service.contentCreatorService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import java.util.*

object ContentClientManager {
    private val clients = setOf(ModernCoreTwitchClient)

    suspend fun startAll(pluginScope: CoroutineScope) {
        clients.forEach {
            it.build(pluginScope)
            it.registerStateChangeListener()
        }
    }

    fun closeAll() {
        clients.forEach { it.close() }
    }

    @JvmName("enableStreamEventListenerUUIDs")
    suspend fun enableStreamEventListener(uuids: ObjectSet<UUID>) {
        val contentCreators = contentCreatorService.getContentCreators(uuids)
        enableStreamEventListener(contentCreators)
    }

    @JvmName("enableStreamEventListenerContentCreators")
    suspend fun enableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        clients.forEach { it.enableStreamEventListener(contentCreators) }
    }

    @JvmName("disableStreamEventListenerUUIDs")
    suspend fun disableStreamEventListener(uuids: ObjectSet<UUID>) {
        val contentCreators = contentCreatorService.getContentCreators(uuids)
        disableStreamEventListener(contentCreators)
    }

    @JvmName("disableStreamEventListenerContentCreators")
    suspend fun disableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        clients.forEach { it.disableStreamEventListener(contentCreators) }
    }
}