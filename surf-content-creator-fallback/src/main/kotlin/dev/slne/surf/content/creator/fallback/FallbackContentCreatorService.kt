package dev.slne.surf.content.creator.fallback

import com.google.auto.service.AutoService
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.util.Services.Fallback
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@AutoService(ContentCreatorService::class)
class FallbackContentCreatorService : ContentCreatorService, Fallback {

    private val _fallbackContentCreators = mutableObjectSetOf<FallbackContentCreator>()
    private val _contentCreators = mutableObjectSetOf<ContentCreator>()

    override val contentCreators get() = _contentCreators

    override suspend fun fetchContentCreators() = newSuspendedTransaction(Dispatchers.IO) {
        _fallbackContentCreators.clear()
        _contentCreators.clear()

        _fallbackContentCreators.addAll(FallbackContentCreator.all())
        _contentCreators.addAll(_fallbackContentCreators.map { it.toContentCreator() })

        _contentCreators
    }
}