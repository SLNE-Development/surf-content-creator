package dev.slne.surf.content.creator.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.content.creator.api.ContentCreator
import dev.slne.surf.content.creator.api.ContentCreatorPlattform
import dev.slne.surf.content.creator.api.api
import dev.slne.surf.content.creator.api.listener.StateChangeListener
import dev.slne.surf.content.creator.api.plattform.PlattformState
import dev.slne.surf.content.creator.core.service.contentCreatorService
import dev.slne.surf.content.creator.core.twitch.CoreTwitchClient
import dev.slne.surf.content.creator.fallback.FallbackContentCreatorTable
import dev.slne.surf.database.DatabaseProvider
import kotlinx.coroutines.runBlocking
import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.event.plugin.TabLoadEvent
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.time.measureTime

val plugin get() = VelocityContentCreatorPlugin.instance

class VelocityContentCreatorPlugin @Inject constructor(
    val server: ProxyServer,
    val container: PluginContainer,
    @DataDirectory val dataPath: Path,
    suspendingPluginContainer: SuspendingPluginContainer
) {

    private val logger = ComponentLogger.logger()

    init {
        instance = this
        suspendingPluginContainer.initialize(this)

        DatabaseProvider(dataPath, dataPath / "storage").connect()

        transaction {
            SchemaUtils.create(
                FallbackContentCreatorTable
            )

            runBlocking {
                val duration = measureTime {
                    contentCreatorService.fetchContentCreators()
                }

                logger.info("Fetched ${contentCreatorService.contentCreators.size} creators in ${duration.inWholeMilliseconds}ms from the database.")
            }
        }

        CoreTwitchClient.build()
        contentCreatorService.contentCreators.forEach { println(it) }

        api.registerStateChangeListener(object : StateChangeListener {
            override fun onStateChanged(
                contentCreatorPlattform: ContentCreatorPlattform,
                newState: PlattformState
            ) {
//                server.allPlayers.forEach { player ->
//                    player.sendText {
//                        info("${contentCreatorPlattform.name} is now $newState")
//                    }
//                }
            }
        })
    }

    @Subscribe(order = PostOrder.LATE)
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        registerPlaceholder()
        TabAPI.getInstance().eventBus!!.register(TabLoadEvent::class.java) { registerPlaceholder() }
    }

    private fun registerPlaceholder() {
        with(TabAPI.getInstance().placeholderManager) {
            registerPlayerPlaceholder("%content_creator_live%", 10000) { player ->
                val velocityPlayer = player.player as Player
                val contentCreator =
                    contentCreatorService.contentCreators.firstOrNull { it.minecraftUuid == velocityPlayer.uniqueId }

                renderLiveTag(contentCreator)
            }
        }
    }

    private fun renderLiveTag(contentCreator: ContentCreator?, space: Boolean? = true): String {
        if (contentCreator == null) {
            return ""
        }

        val twitchLive = contentCreator.twitch?.state == PlattformState.ONLINE
        val youtubeLive = contentCreator.youtube?.state == PlattformState.ONLINE

        return if (twitchLive || youtubeLive) {
            "${if (space == true) " " else ""}§c●§r"
        } else {
            ""
        }
    }

    companion object {
        lateinit var instance: VelocityContentCreatorPlugin
    }

}