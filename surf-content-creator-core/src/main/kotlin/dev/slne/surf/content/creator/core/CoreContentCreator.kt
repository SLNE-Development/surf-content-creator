package dev.slne.surf.content.creator.core

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.ContentCreatorPlatform
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.api.toPlattform
import java.util.*

data class CoreContentCreator(
    override val minecraftUuid: UUID,
) : ContentCreator {
    private val platforms = Caffeine.newBuilder()
        .build<PlatformType, ContentCreatorPlatform?> {
            when(it) {
                PlatformType.TWITCH -> twitchName?.let { PlatformType.TWITCH.toPlattform(it.lowercase()) }
                else -> null
            }
        }

    var twitchName: String? = null
        set(value) {
            field = value
            platforms.put(PlatformType.TWITCH, value?.let { PlatformType.TWITCH.toPlattform(it.lowercase()) })
        }


    override fun getPlatform(type: PlatformType): ContentCreatorPlatform? = platforms.get(type)

    override fun toString(): String {
        return "CoreContentCreator(minecraftUuid=$minecraftUuid, twitchName=$twitchName)"
    }
}