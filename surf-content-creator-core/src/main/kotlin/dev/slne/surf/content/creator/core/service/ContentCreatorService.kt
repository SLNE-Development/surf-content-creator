package dev.slne.surf.content.creator.core.service

import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.ContentCreatorInstance
import dev.slne.surf.content.creator.core.CoreContentCreator
import dev.slne.surf.content.creator.core.client.ModernCoreTwitchClient
import dev.slne.surf.social.api.SurfSocialApi
import dev.slne.surf.social.api.connection.impl.TwitchConnection
import dev.slne.surf.social.api.findConnection
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ContentCreatorService {

    private val coreCreators = ConcurrentHashMap<UUID, CoreContentCreator>()
    private val onlineSessions = ConcurrentHashMap.newKeySet<UUID>()

    val contentCreators get() = coreCreators.values.toObjectSet<ContentCreator>()

    /**
     * Marks the session of [playerUuid] as active. Must be called on the connection thread before
     * the asynchronous [cacheCreator] is started, so that [endSession] can always cancel it out.
     */
    fun beginSession(playerUuid: UUID) {
        onlineSessions.add(playerUuid)
    }

    /**
     * Ends the session of [playerUuid] and returns the creator that was cached for it, if any, so
     * that the caller can still clean up platform listeners for a player that is already gone.
     */
    fun endSession(playerUuid: UUID): CoreContentCreator? {
        onlineSessions.remove(playerUuid)
        return coreCreators.remove(playerUuid)
    }

    suspend fun cacheCreator(playerUuid: UUID) = SurfSocialApi.findConnection<TwitchConnection>(playerUuid)?.let {
        val creator = CoreContentCreator(playerUuid).also { c ->
            c.twitchName = it.twitchName
        }

        coreCreators[playerUuid] = creator

        if (playerUuid !in onlineSessions) {
            coreCreators.remove(playerUuid, creator)
            return@let
        }

        for (type in PlatformType.entries) {
            val platform = creator.getPlatform(type) ?: continue
            if (platform.state == PlatformState.ONLINE) {
                ModernCoreTwitchClient.channelGoLive(platform.name)
            }
        }
    }

    fun invalidate(playerUuid: UUID) {
        endSession(playerUuid)
    }

    /**
     * Forces the state of the [platformType] platform of the content creator behind [playerUuid] to
     * [newState] and notifies every registered state change listener.
     *
     * @return `false` if that player is no known content creator or has no such platform
     */
    fun updatePlatformState(
        playerUuid: UUID,
        platformType: PlatformType,
        newState: PlatformState
    ): Boolean {
        val platform = getContentCreator(playerUuid)?.getPlatform(platformType) ?: return false

        platform.state = newState
        ContentCreatorInstance.callOnStateChangeListener(playerUuid, platform, newState)

        return true
    }

    /**
     * Drops all cached player state.
     */
    fun clear() {
        onlineSessions.clear()
        coreCreators.clear()
    }

    /**
     * Whether the session of [playerUuid] is still active.
     */
    fun isSessionActive(playerUuid: UUID): Boolean = playerUuid in onlineSessions

    fun getContentCreator(uuid: UUID): ContentCreator? = coreCreators[uuid]

    /**
     * Returns the creator whose [platformType] platform is named [platformName], or `null` if no
     * cached creator matches.
     */
    fun findByPlatformName(platformType: PlatformType, platformName: String): ContentCreator? {
        for (creator in coreCreators.values) {
            val platform = creator.getPlatform(platformType) ?: continue
            if (platform.name.equals(platformName, ignoreCase = true)) {
                return creator
            }
        }

        return null
    }

    fun getContentCreators(uuids: ObjectSet<UUID>): ObjectSet<ContentCreator> = uuids.mapNotNullTo(
        mutableObjectSetOf()
    ) { getContentCreator(it) }
}
