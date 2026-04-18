package dev.slne.surf.content.creator.core.config

import dev.slne.surf.api.core.config.createSpongeYmlConfig
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.content.creator.core.coreApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import kotlin.time.Duration.Companion.minutes

@ConfigSerializable
data class ContentCreatorConfig(
    val liveTag: String = "<red>●</red>",
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