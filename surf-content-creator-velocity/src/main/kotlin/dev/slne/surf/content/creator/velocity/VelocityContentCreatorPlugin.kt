package dev.slne.surf.content.creator.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.scope
import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.content.creator.core.client.ContentClientManager
import dev.slne.surf.content.creator.core.service.contentCreatorService
import dev.slne.surf.database.DatabaseProvider
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.time.measureTime

val plugin get() = VelocityContentCreatorPlugin.instance

class VelocityContentCreatorPlugin @Inject constructor(
    val server: ProxyServer,
    @DataDirectory val dataPath: Path,
    suspendingPluginContainer: SuspendingPluginContainer
) {

    private val logger = ComponentLogger.logger()

    @Inject
    lateinit var pluginContainer: PluginContainer

    init {
        instance = this
        suspendingPluginContainer.initialize(this)

        DatabaseProvider(dataPath, dataPath / "storage").connect()

        transaction {
            runBlocking {
                val duration = measureTime {
                    contentCreatorService.fetchContentCreators()
                }

                logger.info("Fetched ${contentCreatorService.contentCreators.size} creators in ${duration.inWholeMilliseconds}ms from the database.")
            }
        }
    }

    @Subscribe(order = PostOrder.LATE)
    suspend fun onProxyInitialization(event: ProxyInitializeEvent) {
        ContentClientManager.startAll(pluginContainer.scope)

        ContentClientManager.enableStreamEventListener(server.allPlayers.mapTo(mutableObjectSetOf()) { it.uniqueId })
        PlaceholderManager.register()

    }

    @Subscribe(order = PostOrder.LATE)
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        ContentClientManager.closeAll()
    }

    @Subscribe(order = PostOrder.LATE)
    suspend fun onUserConnect(event: PostLoginEvent) {
        ContentClientManager.enableStreamEventListener(event.player.uniqueId)
    }

    @Subscribe(order = PostOrder.LATE)
    suspend fun onUserDisconnect(event: DisconnectEvent) {
        ContentClientManager.disableStreamEventListener(event.player.uniqueId)
    }

    companion object {
        lateinit var instance: VelocityContentCreatorPlugin
    }

}