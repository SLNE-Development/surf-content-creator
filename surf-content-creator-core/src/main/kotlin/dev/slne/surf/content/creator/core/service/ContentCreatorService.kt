package dev.slne.surf.content.creator.core.service

import com.github.benmanes.caffeine.cache.Caffeine
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

object ContentCreatorService {
    private val coreCreators = Caffeine.newBuilder().build<UUID, CoreContentCreator>()
    val contentCreators get() = coreCreators.asMap().values.toObjectSet<ContentCreator>()

    suspend fun cacheCreator(playerUuid: UUID) = SurfSocialApi.findConnection<TwitchConnection>(playerUuid)?.let {
        val creator = CoreContentCreator(playerUuid).also { c ->
            c.twitchName = it.twitchName
        }

        coreCreators.put(playerUuid, creator)

        PlatformType.entries.mapNotNull { type -> creator.getPlatform(type) }.filter { it.state == PlatformState.ONLINE }.forEach { platform ->
            ModernCoreTwitchClient.channelGoLive(platform.name)
        }
    }

    fun invalidate(playerUuid: UUID) {
        coreCreators.invalidate(playerUuid)
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

    fun getContentCreator(uuid: UUID): ContentCreator? = coreCreators.getIfPresent(uuid)
    fun getContentCreators(uuids: ObjectSet<UUID>): ObjectSet<ContentCreator> = uuids.mapNotNullTo(
        mutableObjectSetOf()
    ) { getContentCreator(it) }
}