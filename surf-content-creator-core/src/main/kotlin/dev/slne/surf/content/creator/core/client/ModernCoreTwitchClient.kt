package dev.slne.surf.content.creator.core.client

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.events.ChannelGoLiveEvent
import com.github.twitch4j.events.ChannelGoOfflineEvent
import com.github.twitch4j.helix.domain.Stream
import com.netflix.hystrix.exception.HystrixRuntimeException
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.config.config
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

object ModernCoreTwitchClient : ContentClient(PlatformType.TWITCH) {
    private const val BATCH_SIZE = 100

    private const val REQUEST_PARALLELISM = 8

    @Volatile
    private lateinit var twitchClient: TwitchClient

    private val twitchUserIds = ConcurrentHashMap<String, String>()

    override val ioDispatcher: CoroutineDispatcher =
        Dispatchers.IO.limitedParallelism(REQUEST_PARALLELISM)

    override suspend fun build(pluginScope: CoroutineScope) {
        twitchClient = TwitchClientBuilder.builder()
            .withEnableHelix(true)
            .withClientId(config.twitch.clientId)
            .withClientSecret(config.twitch.clientSecret)
            .build()

        log.atInfo()
            .log("Fetching streams for all content creators. This may take a while...")

        val duration = measureTimeMillis {
            updateStreamers(ContentCreatorService.contentCreators)
        }

        log.atInfo()
            .log("Fetched streams in ${duration}ms")
    }

    override fun registerStateChangeListener() {
        with(twitchClient.eventManager) {
            onEvent(ChannelGoLiveEvent::class.java) { channelGoLive(it.channel.name) }
            onEvent(ChannelGoOfflineEvent::class.java) { channelGoOffline(it.channel.name) }
        }
    }

    override suspend fun enableStreamEventListener(contentCreator: ContentCreator) {
        val channelName = contentCreator.getPlatform(PlatformType.TWITCH)?.name ?: return

        withContext(ioDispatcher) {
            val user = try {
                twitchClient.clientHelper.enableStreamEventListener(channelName)
            } catch (e: HystrixRuntimeException) {
                if (e.failureType != HystrixRuntimeException.FailureType.BAD_REQUEST_EXCEPTION) {
                    throw e
                }

                log.atWarning()
                    .log(
                        "Failed to enable stream event listener for channel: $channelName. " +
                                "This is likely due to a bad request, possibly an invalid channel name."
                    )
                return@withContext
            }

            user?.id?.let { twitchUserIds[channelName] = it }

            if (!ContentCreatorService.isSessionActive(contentCreator.minecraftUuid)) {
                disableChannel(channelName)
                return@withContext
            }

            updateStreamers(ObjectSet.of(contentCreator))
        }
    }

    override suspend fun enableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        val channelNames = contentCreators.mapNotNull {
            it.getPlatform(
                PlatformType.TWITCH
            )?.name
        }

        withContext(ioDispatcher) {
            val successUser = twitchClient.clientHelper.enableStreamEventListener(channelNames)
            successUser.forEach { twitchUserIds[it.login] = it.id }

            val failedChannelNames = channelNames - successUser.map { it.login }.toSet()
            if (failedChannelNames.isNotEmpty()) {
                log.atWarning()
                    .log("Failed to enable stream event listener for channels: $failedChannelNames")
            }

            updateStreamers(contentCreators)
        }
    }

    override suspend fun disableStreamEventListener(contentCreator: ContentCreator) {
        val channelName = contentCreator.getPlatform(PlatformType.TWITCH)?.name ?: return

        withContext(ioDispatcher) {
            disableChannel(channelName)
        }
    }

    override suspend fun disableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        val channelNames = contentCreators.mapNotNull {
            it.getPlatform(
                PlatformType.TWITCH
            )?.name
        }

        withContext(ioDispatcher) {
            val helper = twitchClient.clientHelper
            val unresolved = ArrayList<String>()

            for (channelName in channelNames) {
                val userId = twitchUserIds.remove(channelName)
                if (userId != null) {
                    helper.disableStreamEventListenerForId(userId)
                } else {
                    unresolved += channelName
                }
            }

            if (unresolved.isNotEmpty()) {
                helper.disableStreamEventListener(unresolved)
            }
        }
    }

    private fun disableChannel(channelName: String) {
        val userId = twitchUserIds.remove(channelName)
        if (userId != null) {
            twitchClient.clientHelper.disableStreamEventListenerForId(userId)
            return
        }

        try {
            twitchClient.clientHelper.disableStreamEventListener(channelName)
        } catch (e: HystrixRuntimeException) {
            if (e.failureType != HystrixRuntimeException.FailureType.BAD_REQUEST_EXCEPTION) {
                throw e
            }

            log.atWarning()
                .log(
                    "Failed to disable stream event listener for channel: $channelName. " +
                            "This is likely due to a bad request, possibly an invalid channel name."
                )
        }
    }

    override suspend fun buildStreamMap(contentCreators: ObjectSet<out ContentCreator>): Map<String, Stream> {
        val allNames = contentCreators.mapNotNull { it.getPlatform(PlatformType.TWITCH)?.name }
        val (validNames, invalidNames) = partitionTwitchNames(allNames)

        if (invalidNames.isNotEmpty()) {
            log.atWarning()
                .log("Skipping invalid Twitch names: $invalidNames")
        }

        if (validNames.isEmpty()) {
            return emptyMap()
        }

        val batches = validNames.chunked(BATCH_SIZE)

        val batchedStreams = coroutineScope {
            batches.map { batch -> async { fetchStreams(batch) } }.awaitAll()
        }

        val streams = LinkedHashMap<String, Stream>()
        for (batch in batchedStreams) {
            for (stream in batch) {
                streams[stream.userLogin] = stream
            }
        }

        return streams
    }

    private fun fetchStreams(logins: List<String>): List<Stream> = twitchClient.helix
        .getStreams(null, null, null, BATCH_SIZE, null, null, null, logins)
        .execute()
        .streams

    override fun close() {
        twitchUserIds.clear()
        twitchClient.close()
    }
}
