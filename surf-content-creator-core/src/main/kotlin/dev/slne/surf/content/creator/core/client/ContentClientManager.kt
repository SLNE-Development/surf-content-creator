package dev.slne.surf.content.creator.core.client

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import java.util.*

object ContentClientManager {
    private val log = logger()

    private val clients = setOf(ModernCoreTwitchClient)

    suspend fun startAll(pluginScope: CoroutineScope) {
        clients.forEach {
            it.build(pluginScope)
            it.registerStateChangeListener()
            it.startRefresh(pluginScope)
        }
    }

    fun closeAll() {
        clients.forEach { client ->
            try {
                client.close()
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to close content client ${client.javaClass.simpleName}.")
            }
        }
    }

    @JvmName("enableStreamEventListenerUUIDs")
    suspend fun enableStreamEventListener(uuids: ObjectSet<UUID>) {
        val contentCreators = ContentCreatorService.getContentCreators(uuids)
        enableStreamEventListener(contentCreators)
    }

    suspend fun enableStreamEventListener(uuid: UUID) {
        val contentCreator = ContentCreatorService.getContentCreator(uuid) ?: return
        enableStreamEventListener(contentCreator)
    }

    @JvmName("enableStreamEventListenerContentCreators")
    suspend fun enableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        forEachClient("enable stream event listeners") { it.enableStreamEventListener(contentCreators) }
    }

    suspend fun enableStreamEventListener(contentCreator: ContentCreator) {
        forEachClient("enable stream event listener for $contentCreator") {
            it.enableStreamEventListener(contentCreator)
        }
    }

    @JvmName("disableStreamEventListenerUUIDs")
    suspend fun disableStreamEventListener(uuids: ObjectSet<UUID>) {
        val contentCreators = ContentCreatorService.getContentCreators(uuids)
        disableStreamEventListener(contentCreators)
    }

    @JvmName("disableStreamEventListenerContentCreators")
    suspend fun disableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        forEachClient("disable stream event listeners") { it.disableStreamEventListener(contentCreators) }
    }

    suspend fun disableStreamEventListener(uuid: UUID) {
        val contentCreator = ContentCreatorService.getContentCreator(uuid) ?: return
        disableStreamEventListener(contentCreator)
    }

    suspend fun disableStreamEventListener(contentCreator: ContentCreator) {
        forEachClient("disable stream event listener for $contentCreator") {
            it.disableStreamEventListener(contentCreator)
        }
    }

    private suspend fun forEachClient(action: String, block: suspend (ContentClient) -> Unit) {
        for (client in clients) {
            try {
                block(client)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to $action on ${client.javaClass.simpleName}.")
            }
        }
    }
}
