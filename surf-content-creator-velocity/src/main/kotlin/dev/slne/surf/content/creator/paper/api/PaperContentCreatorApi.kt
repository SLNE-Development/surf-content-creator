package dev.slne.surf.content.creator.paper.api

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.content.creator.api.ContentCreatorApi
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.CoreContentCreatorApi
import dev.slne.surf.content.creator.core.config.config
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import dev.slne.surf.content.creator.paper.plugin
import net.kyori.adventure.text.Component
import java.nio.file.Path
import java.util.*

@AutoService(ContentCreatorApi::class)
class PaperContentCreatorApi : CoreContentCreatorApi() {
    override val dataPath: Path
        get() = plugin.dataPath

    override fun renderLiveTag(playerUuid: UUID, space: Boolean): Component {
        val creator =
            ContentCreatorService.contentCreators.find { it.minecraftUuid == playerUuid }
                ?: return Component.empty()

        val live = PlatformType.entries
            .map { creator.getPlatform(it) }
            .any { it?.state == PlatformState.ONLINE }

        return buildText {
            if (live) {
                if(space) {
                    appendSpace()
                }
                append(miniMessage.deserialize(config.liveTag))
            } else {
                Component.empty()
            }
        }
    }
}