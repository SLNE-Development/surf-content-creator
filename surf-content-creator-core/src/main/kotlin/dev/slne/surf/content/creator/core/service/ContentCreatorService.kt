package dev.slne.surf.content.creator.core.service

import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.time.Instant
import java.util.UUID

interface ContentCreatorService {

    /**
     * The content creators.
     */
    val contentCreators: ObjectSet<ContentCreator>

    /**
     * Fetches all content creators from the database.
     *
     * @return The content creators.
     */
    suspend fun fetchContentCreators(): ObjectSet<ContentCreator>

    /**
     * Refreshes the content creators from the database since the given time.
     *
     * @param since The time since the content creators should be refreshed.
     * @return The new content creators.
     */
    suspend fun refreshContentCreators(since: Instant): ObjectSet<out ContentCreator>

    fun getContentCreator(uuid: UUID): ContentCreator?
    fun getContentCreators(uuids: ObjectSet<UUID>): ObjectSet<ContentCreator>

    companion object {
        val INSTANCE = requiredService<ContentCreatorService>()
    }
}

/**
 * Get the [ContentCreatorService] instance.
 *
 * @return The [ContentCreatorService] instance.
 */
val contentCreatorService: ContentCreatorService get() = ContentCreatorService.INSTANCE