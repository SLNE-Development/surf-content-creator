package dev.slne.surf.content.creator.core

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.config.config
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import net.kyori.adventure.text.Component
import java.util.*

/**
 * Renders the tag that marks a content creator as currently streaming.
 */
object LiveTagRenderer {

    /**
     * Renders the live tag for the content creator behind [playerUuid], or an empty component if
     * that player is no known content creator.
     *
     * @param space whether the tag is prefixed with a space
     */
    fun render(playerUuid: UUID, space: Boolean): Component {
        val creator =
            ContentCreatorService.contentCreators.find { it.minecraftUuid == playerUuid }
                ?: return Component.empty()

        val live = PlatformType.entries
            .map { creator.getPlatform(it) }
            .any { it?.state == PlatformState.ONLINE }

        return renderTag(live, space, miniMessage.deserialize(config.liveTag))
    }

    internal fun renderTag(live: Boolean, space: Boolean, liveTag: Component): Component =
        buildText {
            if (live) {
                if (space) {
                    appendSpace()
                }
                append(liveTag)
            } else {
                Component.empty()
            }
        }
}
