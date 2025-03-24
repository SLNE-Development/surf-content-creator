package dev.slne.surf.content.creator.velocity

import dev.slne.surf.content.creator.api.ContentCreator

fun ContentCreator.toPlayer() = plugin.server.getPlayer(minecraftUuid)