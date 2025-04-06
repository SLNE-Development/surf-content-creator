package dev.slne.surf.content.creator.fallback

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object FallbackContentCreatorTable : LongIdTable("freebuild_whitelists") {
    val minecraftUuid =
        varchar("uuid", 36).transform({ UUID.fromString(it) }, { it.toString() })
    val twitchName = varchar("twitch_link", 255).nullable()
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }
}