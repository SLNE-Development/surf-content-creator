package dev.slne.surf.content.creator.fallback

import dev.slne.surf.content.creator.core.CoreContentCreator
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FallbackContentCreator(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FallbackContentCreator>(FallbackContentCreatorTable)

    var name by FallbackContentCreatorTable.name
    var minecraftUuid by FallbackContentCreatorTable.minecraftUuid
    var youtubeName by FallbackContentCreatorTable.youtubeName
    var twitchName by FallbackContentCreatorTable.twitchName

    fun toContentCreator() = CoreContentCreator(
        name,
        minecraftUuid,
        youtubeName,
        twitchName
    )

    override fun toString(): String {
        return toContentCreator().toString()
    }
}