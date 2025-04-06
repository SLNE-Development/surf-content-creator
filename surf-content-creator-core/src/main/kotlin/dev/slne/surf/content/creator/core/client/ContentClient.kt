package dev.slne.surf.content.creator.core.client

import com.github.twitch4j.helix.domain.Stream
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.ContentCreatorInstance
import dev.slne.surf.content.creator.core.service.contentCreatorService
import dev.slne.surf.surfapi.core.api.util.logger
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable

abstract class ContentClient(private val platformType: PlatformType) : Closeable {

    protected val log = logger()

    abstract suspend fun build(pluginScope: CoroutineScope)
    abstract fun registerStateChangeListener()
    abstract suspend fun enableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>)
    abstract suspend fun disableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>)

    protected fun channelGoLive(channelName: String) {
        val platform = contentCreatorService.contentCreators
            .mapNotNull { it.getPlatform(platformType) }
            .find { it.name.equals(channelName, ignoreCase = true) }

        if (platform == null) {
            log.atWarning()
                .log("Channel $channelName went live but is not registered or $platformType data is missing.")
            return
        }

        ContentCreatorInstance.callOnStateChangeListener(platform, PlatformState.ONLINE)
        platform.state = PlatformState.ONLINE
    }

    protected fun channelGoOffline(channelName: String) {
        val platform = contentCreatorService.contentCreators
            .mapNotNull { it.getPlatform(platformType) }
            .find { it.name.equals(channelName, ignoreCase = true) }

        if (platform == null) {
            log.atWarning()
                .log("Channel $channelName went offline but is not registered or $platformType data is missing.")
            return
        }

        ContentCreatorInstance.callOnStateChangeListener(platform, PlatformState.OFFLINE)
        platform.state = PlatformState.OFFLINE
    }

    override fun close() {

    }

    protected open suspend fun updateStreamers(contentCreators: ObjectSet<out ContentCreator>) =
        withContext(Dispatchers.IO) {
            val streamMap = buildStreamMap(contentCreators)
            contentCreators.forEach { creator ->
                val platform = creator.getPlatform(platformType)
                val name = platform?.name
                platform?.state = if (name != null && name in streamMap) {
                    PlatformState.ONLINE
                } else {
                    PlatformState.OFFLINE
                }
            }
        }

    protected abstract suspend fun buildStreamMap(contentCreators: ObjectSet<out ContentCreator>): Map<String, Stream>
}