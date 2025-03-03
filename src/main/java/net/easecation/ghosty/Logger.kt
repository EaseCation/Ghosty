package net.easecation.ghosty

import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.plugin.Plugin
import cn.nukkit.plugin.PluginDescription
import cn.nukkit.plugin.PluginLoader
import cn.nukkit.plugin.PluginLogger
import cn.nukkit.utils.Config
import cn.nukkit.utils.MainLogger
import java.io.File
import java.io.InputStream
import java.lang.invoke.MethodHandles

object Logger {
    @JvmStatic
    fun get(): PluginLogger {
        if (Server.getInstance() == null) {
            return PluginLogger(DummyPlugin)
        }
        return GhostyPlugin.getInstance().logger
    }

    @JvmStatic
    fun getServer(): MainLogger {
        if (Server.getInstance() == null) {
            return MainLogger.getLogger()
        }
        return Server.getInstance().logger
    }
}

private object DummyPlugin: Plugin {
    override fun getDescription(): PluginDescription = PluginDescription("""
        name: Ghosty
        main: net.easecation.ghosty.GhostyPlugin
        version: "0.0.1"
        api: ["1.0.0"]
        load: STARTUP
        author: EaseCation Team
        description: Ghosty - Record player moves and playback when server is empty - like walking ghosts
        website: http://easecation.net
    """.trimIndent())
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean = throw NotImplementedError()
    override fun onLoad() = throw NotImplementedError()
    override fun onEnable() = throw NotImplementedError()
    override fun isEnabled(): Boolean = throw NotImplementedError()
    override fun onDisable() = throw NotImplementedError()
    override fun isDisabled(): Boolean = throw NotImplementedError()
    override fun getDataFolder(): File = throw NotImplementedError()
    override fun getResource(filename: String?): InputStream = throw NotImplementedError()
    override fun saveResource(filename: String?): Boolean = throw NotImplementedError()
    override fun saveResource(filename: String?, replace: Boolean): Boolean = throw NotImplementedError()
    override fun saveResource(filename: String?, outputName: String?, replace: Boolean): Boolean = throw NotImplementedError()
    override fun getConfig(): Config = throw NotImplementedError()
    override fun saveConfig() = throw NotImplementedError()
    override fun saveDefaultConfig() = throw NotImplementedError()
    override fun reloadConfig() = throw NotImplementedError()
    override fun getServer(): Server = throw NotImplementedError()
    override fun getName(): String = throw NotImplementedError()
    override fun getLogger(): PluginLogger = throw NotImplementedError()
    override fun getPluginLoader(): PluginLoader = throw NotImplementedError()
    override fun getMethodHandlesLookup(): MethodHandles.Lookup = throw NotImplementedError()
}
