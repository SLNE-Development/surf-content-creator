package dev.slne.surf.content.creator.api

import java.util.*

interface ContentCreator {

    /**
     * The display name of the content creator.
     */
    val name: String

    /**
     * The UUID of the content creator in minecraft
     */
    val minecraftUuid: UUID

    /**
     * The youtube channel of the content creator if any
     */
    val youtube: ContentCreatorPlattform?

    /**
     * The twitch channel of the content creator if any
     */
    val twitch: ContentCreatorPlattform?
}