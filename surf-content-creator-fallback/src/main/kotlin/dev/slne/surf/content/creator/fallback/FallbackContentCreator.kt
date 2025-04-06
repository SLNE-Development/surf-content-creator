package dev.slne.surf.content.creator.fallback

import dev.slne.surf.content.creator.core.CoreContentCreator
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class FallbackContentCreator(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FallbackContentCreator>(FallbackContentCreatorTable)

    var minecraftUuid by FallbackContentCreatorTable.minecraftUuid
    var twitchName by FallbackContentCreatorTable.twitchName

    suspend fun toContentCreator(): CoreContentCreator = newSuspendedTransaction{
        CoreContentCreator(minecraftUuid).also {
            it.twitchName = twitchName
        }
    }

    override fun toString(): String {
        return "FallbackContentCreator(twitchName=$twitchName, minecraftUuid=$minecraftUuid)"
    }
}