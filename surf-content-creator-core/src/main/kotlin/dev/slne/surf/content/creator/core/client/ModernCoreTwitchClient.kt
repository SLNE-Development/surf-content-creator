package dev.slne.surf.content.creator.core.client

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.events.ChannelGoLiveEvent
import com.github.twitch4j.events.ChannelGoOfflineEvent
import com.github.twitch4j.helix.domain.Stream
import com.netflix.hystrix.exception.HystrixRuntimeException
import dev.slne.surf.api.core.util.toObjectList
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.config.config
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.system.measureTimeMillis

object ModernCoreTwitchClient : ContentClient(PlatformType.TWITCH) {
    private const val BATCH_SIZE = 100

    private lateinit var twitchClient: TwitchClient

    override suspend fun build(pluginScope: CoroutineScope): Unit = coroutineScope {
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

        try {
            twitchClient.clientHelper.enableStreamEventListener(channelName)
        } catch (e: HystrixRuntimeException) {
            if (e.failureType != HystrixRuntimeException.FailureType.BAD_REQUEST_EXCEPTION) {
                throw e
            }

            log.atWarning()
                .log("Failed to enable stream event listener for channel: $channelName. " +
                        "This is likely due to a bad request, possibly an invalid channel name.")
            return
        }

        updateStreamers(ObjectSet.of(contentCreator))

    }

    override suspend fun enableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        val channelNames = contentCreators.mapNotNull {
            it.getPlatform(
                PlatformType.TWITCH
            )?.name
        }

        val successUser = twitchClient.clientHelper.enableStreamEventListener(channelNames)
        val failedChannelNames = channelNames - successUser.map { it.login }.toSet()
        if (failedChannelNames.isNotEmpty()) {
            log.atWarning()
                .log("Failed to enable stream event listener for channels: $failedChannelNames")
        }

        updateStreamers(contentCreators)
    }

    override suspend fun disableStreamEventListener(contentCreator: ContentCreator) {
        val channelName = contentCreator.getPlatform(PlatformType.TWITCH)?.name ?: return

        try {
            twitchClient.clientHelper.disableStreamEventListener(channelName)
        } catch (e: HystrixRuntimeException) {
            if (e.failureType != HystrixRuntimeException.FailureType.BAD_REQUEST_EXCEPTION) {
                throw e
            }

            log.atWarning()
                .log("Failed to disable stream event listener for channel: $channelName. " +
                        "This is likely due to a bad request, possibly an invalid channel name.")
            return
        }

        updateStreamers(ObjectSet.of(contentCreator))
    }

    override suspend fun disableStreamEventListener(contentCreators: ObjectSet<out ContentCreator>) {
        val channelNames = contentCreators.mapNotNull {
            it.getPlatform(
                PlatformType.TWITCH
            )?.name
        }

        twitchClient.clientHelper.disableStreamEventListener(channelNames)
    }

    override suspend fun buildStreamMap(contentCreators: ObjectSet<out ContentCreator>): Map<String, Stream> {
        val creatorList = contentCreators.toObjectList()
        val allNames = creatorList.mapNotNull { it.getPlatform(PlatformType.TWITCH)?.name }
        val (validNames, invalidNames) = partitionTwitchNames(allNames)

        if (invalidNames.isNotEmpty()) {
            log.atWarning()
                .log("Skipping invalid Twitch names: $invalidNames")
        }

        val batches = validNames.chunked(BATCH_SIZE)

        // Fetch streams for each batch
        val fetchedStreams = coroutineScope {
            batches.flatMap { batch ->
                val usersResponse = twitchClient.helix
                    .getUsers(null, null, batch)
                    .execute()
                val users = usersResponse.users

                // We need to map the ids to names because the Twitch API returns the userId, not the name
                // and we need to use the name to update the content creator state
                val idToName = users.associate { it.id to it.login }

                val streamResponse = twitchClient.helix
                    .getStreams(null, null, null, BATCH_SIZE, null, null, users.map { it.id }, null)
                    .execute()

                // Filter out streams that are not in the batch
                streamResponse.streams.mapNotNull { stream ->
                    val lowerName = idToName[stream.userId]
                    lowerName?.let { ln -> ln to stream }
                }

            }

        }

        // Create map of userName → Stream
        return fetchedStreams.toMap()
    }

    override fun close() {
        twitchClient.close()
    }
}