package dev.slne.surf.content.creator.core.service

import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet

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