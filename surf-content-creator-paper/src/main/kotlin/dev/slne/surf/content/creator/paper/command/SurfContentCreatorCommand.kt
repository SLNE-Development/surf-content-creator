package dev.slne.surf.content.creator.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.core.ContentCreatorPermissions
import dev.slne.surf.content.creator.core.command.LiveStateDebugCommand

fun surfContentCreatorCommand() = commandTree("surfcontentcreator") {
    withPermission(ContentCreatorPermissions.COMMAND)

    literalArgument("debug") {
        literalArgument("live") {
            playerExecutor { player, _ ->
                LiveStateDebugCommand.setTwitchState(
                    player,
                    player.uniqueId,
                    PlatformState.ONLINE
                )
            }
        }

        literalArgument("offline") {
            playerExecutor { player, _ ->
                LiveStateDebugCommand.setTwitchState(
                    player,
                    player.uniqueId,
                    PlatformState.OFFLINE
                )
            }
        }
    }
}
