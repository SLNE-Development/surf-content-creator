package dev.slne.surf.content.creator.core.twitch

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.events.ChannelGoLiveEvent
import com.github.twitch4j.events.ChannelGoOfflineEvent
import com.github.twitch4j.helix.domain.Stream
import dev.slne.surf.content.creator.api.plattform.PlattformState
import dev.slne.surf.content.creator.core.ContentCreatorInstance
import dev.slne.surf.content.creator.core.service.contentCreatorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import kotlin.time.measureTime

object CoreTwitchClient {

    private val logger = ComponentLogger.logger()

    private val batchSize = 100
    private lateinit var twitchClient: TwitchClient

    fun build() {
        twitchClient = TwitchClientBuilder
            .builder()
            .withEnableHelix(true)
            .withDefaultAuthToken(OAuth2Credential("twitch", "abcd"))
            .build()

        twitchClient.eventManager.onEvent(ChannelGoLiveEvent::class.java) { event ->
            val contentCreator =
                contentCreatorService.contentCreators.find { it.twitch?.name == event.channel.name }

            if (contentCreator == null) {
                logger.error("Channel ${event.channel.name} went live but is not registered.")
                return@onEvent
            }

            val twitch = contentCreator.twitch
            if (twitch == null) {
                logger.error("Channel ${event.channel.name} went live but twitch is not registered.")
                return@onEvent
            }

            ContentCreatorInstance.callOnStateChangeListener(twitch, PlattformState.ONLINE)
            twitch.state = PlattformState.ONLINE

            logger.info("Channel ${event.channel.name} went live setting state.")
        }

        twitchClient.eventManager.onEvent(ChannelGoOfflineEvent::class.java) { event ->
            val contentCreator =
                contentCreatorService.contentCreators.find { it.twitch?.name == event.channel.name }

            if (contentCreator == null) {
                logger.error("Channel ${event.channel.name} went offline but is not registered.")
                return@onEvent
            }

            val twitch = contentCreator.twitch
            if (twitch == null) {
                logger.error("Channel ${event.channel.name} went offline but twitch is not registered.")
                return@onEvent
            }

            ContentCreatorInstance.callOnStateChangeListener(twitch, PlattformState.OFFLINE)
            twitch.state = PlattformState.OFFLINE

            logger.info("Channel ${event.channel.name} went offline setting state.")
        }

        runBlocking {
            val duration = measureTime {
                fetchStreamers()
            }

            logger.info("Fetched streams in ${duration.inWholeMilliseconds}ms")

            contentCreatorService.contentCreators.forEach {
                it.twitch?.let { twitch ->
                    twitchClient.clientHelper.enableStreamEventListener(twitch.name)
                }
            }
        }
    }

    private suspend fun fetchStreamers() = withContext(Dispatchers.IO) {
        val contentCreators = contentCreatorService.contentCreators.toList()
        val fetchedStreams = mutableListOf<Stream>()

        for (i in contentCreators.indices step batchSize) {
            val batch = contentCreators
                .subList(i, (i + batchSize).coerceAtMost(contentCreators.size))
                .mapNotNull { it.twitch?.name }

            val streams = twitchClient
                .helix
                .getStreams(
                    "abcd",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    batch,
                )
                .execute()

            fetchedStreams.addAll(streams.streams)
        }

        contentCreators.forEach {
            val stream = fetchedStreams.find { stream -> stream.userName == it.twitch?.name }

            if (stream != null) {
                it.twitch?.state = PlattformState.ONLINE
            } else {
                it.twitch?.state = PlattformState.OFFLINE
            }
        }
    }

    fun disconnect() {
        twitchClient.close()
    }

}