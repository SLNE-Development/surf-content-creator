package dev.slne.surf.content.creator.core.client

import com.github.twitch4j.helix.domain.Stream
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.ContentCreatorInstance
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable

abstract class ContentClient(private val platformType: PlatformType) : Closeable {
    protected val log = logger()

    /**
     * The dispatcher every blocking platform call of this client runs on. Subclasses override it to
     * bound how much of the shared IO dispatcher they are allowed to occupy.
     */
    protected open val ioDispatcher: CoroutineDispatcher get() = Dispatchers.IO

    abstract suspend fun build(pluginScope: CoroutineScope)
    abstract fun registerStateChangeListener()
    abstract suspend fun enableStreamEventListener(contentCreator: ContentCreator)
    abstract suspend fun enableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>)
    abstract suspend fun disableStreamEventListener(contentCreator: ContentCreator)
    abstract suspend fun disableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>)

    fun channelGoLive(channelName: String) = changeChannelState(channelName, PlatformState.ONLINE)

    protected fun channelGoOffline(channelName: String) =
        changeChannelState(channelName, PlatformState.OFFLINE)

    private fun changeChannelState(channelName: String, newState: PlatformState) {
        val contentCreator =
            ContentCreatorService.findByPlatformName(platformType, channelName) ?: return
        val platform = contentCreator.getPlatform(platformType) ?: return

        platform.state = newState
        ContentCreatorInstance.callOnStateChangeListener(
            contentCreator.minecraftUuid,
            platform,
            newState
        )
    }

    override fun close() {

    }

    protected open suspend fun updateStreamers(contentCreators: ObjectSet<out ContentCreator>) =
        withContext(ioDispatcher) {
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
