package dev.slne.surf.content.creator.velocity

import com.velocitypowered.api.proxy.Player
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.config.config
import dev.slne.surf.content.creator.core.service.contentCreatorService
import io.github.miniplaceholders.api.Expansion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag

object PlaceholderManager {

    fun register() {
        Expansion.builder("content_creator")
            .filter(Player::class.java)
            .audiencePlaceholder("live") { audience, _, _ ->
                renderLiveTag(audience as Player)
            }
            .audiencePlaceholder("live_space") { audience, _, _ ->
                renderLiveTag(audience as Player, true)
            }
            .build()
            .register()
    }

    private fun renderLiveTag(player: Player, space: Boolean? = true): Tag {
        val creator =
            contentCreatorService.contentCreators.find { it.minecraftUuid == player.uniqueId }
                ?: return Tag.inserting(Component.empty())

        val live = PlatformType.entries
            .map { creator.getPlatform(it) }
            .any { it?.state == PlatformState.ONLINE }

        return Tag.preProcessParsed(
            if (live) {
                (if (space == true) " " else "") + config.liveTag
            } else {
                ""
            }
        )
    }
}