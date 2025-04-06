package dev.slne.surf.content.creator.fallback

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.auto.service.AutoService
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.core.CoreContentCreator
import dev.slne.surf.content.creator.core.service.ContentCreatorService
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.util.Services.Fallback
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.*

@Suppress("unused")
@AutoService(ContentCreatorService::class)
class FallbackContentCreatorService : ContentCreatorService, Fallback {
    private val log = logger()

    private val coreCreators = Caffeine.newBuilder()
        .build<UUID, CoreContentCreator>()

    override val contentCreators
        get() = coreCreators.asMap().values.toObjectSet<ContentCreator>()

    override suspend fun fetchContentCreators() = newSuspendedTransaction(Dispatchers.IO) {
        coreCreators.invalidateAll()
        FallbackContentCreator.all().forEach {
            val core = CoreContentCreator(it.minecraftUuid).also { c ->
                c.twitchName = it.twitchName
            }
            coreCreators.put(it.minecraftUuid, core)
        }
        contentCreators
    }

    override suspend fun refreshContentCreators(since: Instant) = newSuspendedTransaction(Dispatchers.IO) {
        FallbackContentCreator.find(FallbackContentCreatorTable.updatedAt greaterEq since)
        .onEach {
            val core = CoreContentCreator(it.minecraftUuid).also { c ->
                c.twitchName = it.twitchName
            }
            coreCreators.put(it.minecraftUuid, core)
        }
        .map { uuidToCore(it) }
        .toObjectSet()
    }

    override fun getContentCreator(uuid: UUID): ContentCreator? {
        return coreCreators.getIfPresent(uuid)
    }

    override fun getContentCreators(uuids: ObjectSet<UUID>): ObjectSet<ContentCreator> {
        return uuids.mapNotNullTo(mutableObjectSetOf()) { coreCreators.getIfPresent(it) }
    }

    private fun uuidToCore(fc: FallbackContentCreator): CoreContentCreator {
        return coreCreators.getIfPresent(fc.minecraftUuid)
            ?: CoreContentCreator(fc.minecraftUuid).also { c ->
                c.twitchName = fc.twitchName
            }
    }
}
