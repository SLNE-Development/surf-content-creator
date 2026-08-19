package dev.slne.surf.content.creator.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.content.creator.core.ContentCreatorLifecycle
import dev.slne.surf.content.creator.paper.command.surfContentCreatorCommand
import dev.slne.surf.content.creator.paper.listener.PlayerConnectionListener
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
        ContentCreatorLifecycle.enable()

        pluginManager.registerEvents(PlayerConnectionListener, this)

        surfContentCreatorCommand()
    }

    override suspend fun onDisableAsync() {
        ContentCreatorLifecycle.disable()
    }
}
