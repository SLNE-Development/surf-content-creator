package dev.slne.surf.content.creator.minestom.platform

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.coroutine.minestomAsyncScope
import dev.slne.minestom.lobby.api.extension.ConnectionManager
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.content.creator.core.platform.ContentCreatorPlatformBridge
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import java.util.*

@AutoService(ContentCreatorPlatformBridge::class)
class MinestomContentCreatorPlatformBridge : ContentCreatorPlatformBridge {
    override val pluginScope: CoroutineScope
        get() = minestomAsyncScope

    override fun onlinePlayerUuids(): ObjectSet<UUID> =
        ConnectionManager.onlinePlayers.mapTo(mutableObjectSetOf()) { it.uuid }
}
