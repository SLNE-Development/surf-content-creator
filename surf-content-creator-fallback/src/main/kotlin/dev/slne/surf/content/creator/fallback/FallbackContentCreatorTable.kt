package dev.slne.surf.content.creator.fallback

import org.jetbrains.exposed.dao.id.LongIdTable
import java.util.*

object FallbackContentCreatorTable : LongIdTable("content_creators") {
    val name = varchar("name", 255)
    val minecraftUuid =
        varchar("minecraft_uuid", 36).transform({ UUID.fromString(it) }, { it.toString() })
    val youtubeName = varchar("youtube_name", 255).nullable()
    val twitchName = varchar("twitch_name", 255).nullable()
}