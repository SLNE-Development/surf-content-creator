package dev.slne.surf.content.creator.core.platform

import dev.slne.surf.api.core.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import java.util.*

/**
 * Gives platform-neutral code access to the few things only the running platform can answer.
 */
interface ContentCreatorPlatformBridge {

    val pluginScope: CoroutineScope

    /**
     * The minecraft uuids of all players currently connected to this server.
     */
    fun onlinePlayerUuids(): ObjectSet<UUID>
}

val platformBridge by lazy { requiredService<ContentCreatorPlatformBridge>() }
