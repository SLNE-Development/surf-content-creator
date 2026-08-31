package dev.slne.surf.content.creator.core.client

import com.github.twitch4j.helix.domain.Stream
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.runAtFixedRate
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.ContentCreatorPlatform
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.ContentCreatorInstance
import dev.slne.surf.content.creator.core.config.config
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.*
import java.io.Closeable
import kotlin.time.Duration.Companion.seconds

abstract class ContentClient(private val platformType: PlatformType) : Closeable {
    protected val log = logger()

    @Volatile
    private var refreshJob: Job? = null

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

        applyState(contentCreator, platform, newState)
    }

    protected fun applyState(
        contentCreator: ContentCreator,
        platform: ContentCreatorPlatform,
        newState: PlatformState
    ): Boolean {
        synchronized(platform) {
            if (platform.state == newState) {
                return false
            }

            platform.state = newState
        }

        ContentCreatorInstance.callOnStateChangeListener(
            contentCreator.minecraftUuid,
            platform,
            newState
        )

        return true
    }

    fun startRefresh(pluginScope: CoroutineScope) {
        val intervalSeconds = config.liveStateRefreshSeconds

        stopRefreshing()

        if (intervalSeconds <= 0) {
            log.atWarning()
                .log(
                    "Periodic live state refresh is disabled (liveStateRefreshSeconds=$intervalSeconds). " +
                            "Live states will only be corrected when a player reconnects."
                )
            return
        }

        val interval = intervalSeconds.seconds
        refreshJob = pluginScope.runAtFixedRate(interval) {
            try {
                updateStreamers(ContentCreatorService.contentCreators)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to refresh live states. Retrying in ${intervalSeconds}s.")
            }
        }
    }

    private fun stopRefreshing() {
        refreshJob?.cancel()
        refreshJob = null
    }

    override fun close() {
        stopRefreshing()
    }

    protected open suspend fun updateStreamers(contentCreators: ObjectSet<out ContentCreator>) =
        withContext(ioDispatcher) {
            if (contentCreators.isEmpty()) {
                return@withContext
            }

            val lookup = lookupStreams(contentCreators)

            for (creator in contentCreators) {
                val platform = creator.getPlatform(platformType) ?: continue

                if (platform.name !in lookup.checkedNames) {
                    continue
                }

                val newState = if (platform.name in lookup.streams) {
                    PlatformState.ONLINE
                } else {
                    PlatformState.OFFLINE
                }

                applyState(creator, platform, newState)
            }
        }

    protected abstract suspend fun lookupStreams(contentCreators: ObjectSet<out ContentCreator>): StreamLookup
}

data class StreamLookup(
    val streams: Map<String, Stream>,
    val checkedNames: Set<String>
) {
    companion object {
        val EMPTY = StreamLookup(emptyMap(), emptySet())
    }
}
