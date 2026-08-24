package dev.slne.surf.content.creator.core

import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.ContentCreatorPlatform
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.api.toPlattform
import java.util.*

data class CoreContentCreator(
    override val minecraftUuid: UUID,
) : ContentCreator {

    @Volatile
    private var twitchPlatform: ContentCreatorPlatform? = null

    @Volatile
    var twitchName: String? = null
        set(value) {
            twitchPlatform = value?.let { PlatformType.TWITCH.toPlattform(it.lowercase()) }
            field = value
        }

    override fun getPlatform(type: PlatformType): ContentCreatorPlatform? = when (type) {
        PlatformType.TWITCH -> twitchPlatform
    }

    override fun toString(): String {
        return "CoreContentCreator(minecraftUuid=$minecraftUuid, twitchName=$twitchName)"
    }
}
