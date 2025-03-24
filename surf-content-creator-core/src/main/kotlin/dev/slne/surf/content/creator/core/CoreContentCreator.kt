package dev.slne.surf.content.creator.core

import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.ContentCreatorPlattform
import dev.slne.surf.content.creator.api.plattform.PlattformState
import dev.slne.surf.content.creator.api.plattform.PlattformType
import java.util.*

data class CoreContentCreator(
    override val name: String,
    override val minecraftUuid: UUID,

    val youtubeName: String?,
    val twitchName: String?
) : ContentCreator {

    override val twitch = twitchName?.let {
        ContentCreatorPlattform(
            name = it,
            plattform = PlattformType.TWITCH,
            state = PlattformState.UNKNOWN
        )
    }

    override val youtube = youtubeName?.let {
        ContentCreatorPlattform(
            name = it,
            plattform = PlattformType.YOUTUBE,
            state = PlattformState.UNKNOWN
        )
    }

    override fun toString(): String {
        return "CoreContentCreator(name='$name', youtubeName=$youtubeName, twitchName=$twitchName, twitch=$twitch, youtube=$youtube)"
    }

}