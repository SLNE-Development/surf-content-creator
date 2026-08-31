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
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

object ModernCoreTwitchClient : ContentClient(PlatformType.TWITCH) {
    private const val BATCH_SIZE = 100

    private const val REQUEST_PARALLELISM = 8

    private const val MAX_FETCH_ATTEMPTS = 3

    private const val RETRY_BASE_DELAY_MILLIS = 500L

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
            onEvent(ChannelGoLiveEvent::class.java) { event ->
                handleChannelEvent("go live", event.channel.name) { channelGoLive(it) }
            }
            onEvent(ChannelGoOfflineEvent::class.java) { event ->
                handleChannelEvent("go offline", event.channel.name) { channelGoOffline(it) }
            }
        }
    }

    private inline fun handleChannelEvent(
        eventName: String,
        channelName: String,
        handle: (String) -> Unit
    ) {
        try {
            handle(channelName)
        } catch (e: Exception) {
            log.atWarning()
                .withCause(e)
                .log("Failed to handle $eventName event for channel: $channelName")
        }
    }

    override suspend fun enableStreamEventListener(contentCreator: ContentCreator) {
        val channelName = contentCreator.getPlatform(PlatformType.TWITCH)?.name ?: return

        withContext(ioDispatcher) {
            if (!ContentCreatorService.isSessionActive(contentCreator.minecraftUuid)) {
                return@withContext
            }

            updateStreamers(ObjectSet.of(contentCreator))

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
            }
        }
    }

    override suspend fun enableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        val channelNames = contentCreators.mapNotNull {
            it.getPlatform(
                PlatformType.TWITCH
            )?.name
        }

        withContext(ioDispatcher) {
            updateStreamers(contentCreators)

            if (channelNames.isEmpty()) {
                return@withContext
            }

            val successUser = try {
                twitchClient.clientHelper.enableStreamEventListener(channelNames)
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log(
                        "Failed to enable stream event listeners for ${channelNames.size} channels. " +
                                "Their live state is still kept up to date by the periodic reconciliation."
                    )
                return@withContext
            }

            successUser.forEach { twitchUserIds[it.login] = it.id }

            val failedChannelNames = channelNames - successUser.map { it.login }.toSet()
            if (failedChannelNames.isNotEmpty()) {
                log.atWarning()
                    .log("Failed to enable stream event listener for channels: $failedChannelNames")
            }
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

    override suspend fun lookupStreams(contentCreators: ObjectSet<out ContentCreator>): StreamLookup {
        val allNames = contentCreators.mapNotNull { it.getPlatform(PlatformType.TWITCH)?.name }
        val (validNames, invalidNames) = partitionTwitchNames(allNames)

        if (invalidNames.isNotEmpty()) {
            log.atWarning()
                .log("Skipping invalid Twitch names: $invalidNames")
        }

        if (validNames.isEmpty()) {
            return StreamLookup.EMPTY
        }

        val batches = validNames.chunked(BATCH_SIZE)

        val results = coroutineScope {
            batches.map { batch -> async { batch to fetchStreams(batch) } }.awaitAll()
        }

        val streams = LinkedHashMap<String, Stream>()
        val checkedNames = HashSet<String>(validNames.size)

        for ((batch, batchStreams) in results) {
            if (batchStreams == null) {
                continue
            }

            checkedNames += batch

            for (stream in batchStreams) {
                streams[stream.userLogin.lowercase()] = stream
            }
        }

        return StreamLookup(streams, checkedNames)
    }

    private suspend fun fetchStreams(logins: List<String>): List<Stream>? {
        var retryDelayMillis = RETRY_BASE_DELAY_MILLIS

        repeat(MAX_FETCH_ATTEMPTS) { attempt ->
            try {
                return twitchClient.helix
                    .getStreams(null, null, null, BATCH_SIZE, null, null, null, logins)
                    .execute()
                    .streams
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log(
                        "Failed to fetch twitch streams for ${logins.size} channels " +
                                "(attempt ${attempt + 1}/$MAX_FETCH_ATTEMPTS)."
                    )

                if (attempt == MAX_FETCH_ATTEMPTS - 1) {
                    return null
                }

                delay(retryDelayMillis.milliseconds)
                retryDelayMillis *= 2
            }
        }

        return null
    }

    override fun close() {
        super.close()

        twitchUserIds.clear()

        if (::twitchClient.isInitialized) {
            twitchClient.close()
        }
    }
}
