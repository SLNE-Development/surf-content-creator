package dev.slne.surf.content.creator.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.api.platform.PlatformType
import dev.slne.surf.content.creator.core.ContentCreatorInstance
import dev.slne.surf.content.creator.core.service.ContentCreatorService

fun surfContentCreatorCommand() = commandTree("surfcontentcreator") {
    withPermission("surf.contentcreator.command")

    literalArgument("debug") {
        literalArgument("live") {
            playerExecutor { player, _ ->
                val twitchCreator = ContentCreatorService.getContentCreator(player.uniqueId)?.getPlatform(
                    PlatformType.TWITCH)

                if(twitchCreator == null) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Du bist nicht mit Twitch verbunden.")
                    }
                    return@playerExecutor
                }

                twitchCreator.state = PlatformState.ONLINE

                ContentCreatorInstance.callOnStateChangeListener(player.uniqueId, twitchCreator, PlatformState.ONLINE)

                player.sendText {
                    appendSuccessPrefix()
                    success("Der Live-Status wurde erfolgreich auf ONLINE gesetzt.")
                }
            }
        }

        literalArgument("offline") {
            playerExecutor { player, _ ->
                val twitchCreator = ContentCreatorService.getContentCreator(player.uniqueId)?.getPlatform(
                    PlatformType.TWITCH)

                if(twitchCreator == null) {
                    player.sendText {
                        appendErrorPrefix()
                        error("Du bist nicht mit Twitch verbunden.")
                    }
                    return@playerExecutor
                }

                twitchCreator.state = PlatformState.OFFLINE

                ContentCreatorInstance.callOnStateChangeListener(player.uniqueId, twitchCreator, PlatformState.OFFLINE)

                player.sendText {
                    appendSuccessPrefix()
                    success("Der Live-Status wurde erfolgreich auf OFFLINE gesetzt.")
                }
            }
        }
    }
}