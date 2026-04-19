package dev.slne.surf.content.creator.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.scope
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.content.creator.core.client.ContentClientManager
import dev.slne.surf.content.creator.paper.command.surfContentCreatorCommand
import dev.slne.surf.content.creator.paper.listener.PlayerConnectionListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
        ContentClientManager.startAll(plugin.scope)
        ContentClientManager.enableStreamEventListener(Bukkit.getOnlinePlayers().mapTo(mutableObjectSetOf()) { it.uniqueId })

        pluginManager.registerEvents(PlayerConnectionListener, this)

        surfContentCreatorCommand()
    }

    override suspend fun onDisableAsync() {
        ContentClientManager.closeAll()
    }
}