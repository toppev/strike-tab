package ga.strikepractice.striketab

import com.keenant.tabbed.util.Reflection
import ga.strikepractice.striketab.updater.TabUpdateTask
import ga.strikepractice.striketab.util.LEGACY_SUPPORT
import ga.strikepractice.striketab.util.UpdateChecker
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Field

var DEBUG = false
val PREFIX = "${ChatColor.GRAY}[${ChatColor.GREEN}StrikeTab${ChatColor.GRAY}] "
val DEBUG_PREFIX: String = ChatColor.stripColor(PREFIX) + "debug: "

class StrikeTab : JavaPlugin(), CommandExecutor {

    internal lateinit var tabManager: TabManager
    private lateinit var updateChecker: UpdateChecker

    override fun onEnable() {
        super.onEnable()
        saveDefaultConfig()
        getCommand("striketab").executor = this
        initializePlugin()
        updateChecker = UpdateChecker(this)
    }

    private fun initializePlugin() {
        // When reloading the config
        Bukkit.getScheduler().cancelTasks(this)
        HandlerList.unregisterAll(this)

        tabManager = TabManager(this)
        val ticks = config.getLong("tablist.update-ticks")
        TabUpdateTask(tabManager).runTaskTimerAsynchronously(this, ticks, ticks)
    }


    private fun reloadPluginConfig() {
        Bukkit.getLogger().info("Reloading config...")
        reloadConfig()
        LEGACY_SUPPORT = config.getBoolean("legacy-support")
        Bukkit.getLogger().info("Legacy support ${if (LEGACY_SUPPORT) "enabled" else "disabled"}")
        if (LEGACY_SUPPORT && Bukkit.getMaxPlayers() < 60) {
            Bukkit.getLogger().warning(
                "Max player is below 60 (${Bukkit.getMaxPlayers()})! Tab may not work correctly on 1.7 clients!"
            )
        }
        initializePlugin()
        Bukkit.getLogger().info("Plugin config reloaded successfully.")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val admin = sender.isOp || sender.hasPermission("striketab.admin")
        if (!admin && DEBUG) Bukkit.getLogger().info("No perms: striketab.admin")
        if (admin && args.isNotEmpty()) {
            if (arrayOf("reload", "rl").contains(args[0].toLowerCase())) {
                reloadPluginConfig()
                sender.sendMessage("$PREFIX${ChatColor.GREEN}Config reloaded!")
                return true
            }
            if (args[0].equals("debug", true)) {
                DEBUG = !DEBUG
                if (DEBUG) {
                    sender.sendMessage("$PREFIX${ChatColor.RED}Debug logging enabled")
                } else {
                    sender.sendMessage("${PREFIX}Debug logging disabled")
                }
                Bukkit.getLogger().info("StrikeTab debug mode set to $DEBUG")
                return true
            }
            if (args[0].equals("update", true)) {
                Bukkit.getScheduler().runTaskAsynchronously(this) {
                    sender.sendMessage("${ChatColor.GRAY}Checking for updates...")
                    try {
                        updateChecker.checkForUpdates()
                        val messages = updateChecker.joinMessages
                        if (messages.isEmpty()) {
                            sender.sendMessage("${ChatColor.GREEN}No updates found.")
                        } else {
                            messages.forEach { sender.sendMessage(it) }
                        }
                    } catch (e: Exception) {
                        sender.sendMessage("${ChatColor.RED}Failed to check for updates. Check console.")
                        sender.sendMessage("${ChatColor.GRAY}${e.message}")
                        e.printStackTrace()
                    }
                }
                return true
            }
        }
        sender.sendMessage("${PREFIX}${ChatColor.GOLD}StrikeTab ${description.version} - ${this.description.description}")
        if (admin) {
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab reload${ChatColor.GRAY} - reload the config")
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab debug${ChatColor.GRAY} - toggle debug logging")
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab update${ChatColor.GRAY} - check for updates")
        }
        return true
    }

}

/**
 * Translate Bukkit ChatColors
 */
fun String.translateColors(): String = ChatColor.translateAlternateColorCodes('&', this)

inline fun debug(message: () -> String) {
    if (DEBUG) {
        Bukkit.getLogger().info(DEBUG_PREFIX + message.invoke())
    }
}

private lateinit var pingField: Field
fun getPing(player: Player): Int {
    try {
        val craftPlayer = Reflection.getHandle(player)
        if (!::pingField.isInitialized) {
            pingField = craftPlayer.javaClass.getDeclaredField("ping")
        }
        return pingField.getInt(craftPlayer)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}