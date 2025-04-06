package dev.slne.surf.content.creator.core.config

import dev.slne.surf.content.creator.core.coreApi
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.time.Duration.Companion.minutes

@ConfigSerializable
data class ContentCreatorConfig(
    val liveTag: String = "§c●§r",
    val twitch: Twitch = Twitch(),
) {
    @ConfigSerializable
    data class Twitch(
        val clientId: String = "",
        val clientSecret: String = "",
        val refreshIntervalSeconds: Long = 5.minutes.inWholeSeconds,
    )
}

val config by lazy {
    surfConfigApi.createSpongeYmlConfig<ContentCreatorConfig>(
        coreApi.dataPath,
        "config.yml"
    )
}