package dev.slne.surf.content.creator.minestom.command

import dev.slne.minestom.lobby.api.command.CommandRegistrar
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.literalArgument
import dev.slne.minestom.lobby.api.command.commandapi.dsl.playerExecutor
import dev.slne.surf.content.creator.api.platform.PlatformState
import dev.slne.surf.content.creator.core.ContentCreatorPermissions
import dev.slne.surf.content.creator.core.command.LiveStateDebugCommand

class ContentCreatorCommands : CommandRegistrar {

    override fun register() {
        commandTree("surfcontentcreator") {
            withPermission(ContentCreatorPermissions.COMMAND)

            literalArgument("debug") {
                literalArgument("live") {
                    playerExecutor { player, _ ->
                        LiveStateDebugCommand.setTwitchState(
                            player,
                            player.uuid,
                            PlatformState.ONLINE
                        )
                    }
                }

                literalArgument("offline") {
                    playerExecutor { player, _ ->
                        LiveStateDebugCommand.setTwitchState(
                            player,
                            player.uuid,
                            PlatformState.OFFLINE
                        )
                    }
                }
            }
        }
    }
}
