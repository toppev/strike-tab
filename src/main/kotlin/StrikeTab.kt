package ga.strikepractice.striketab

import com.keenant.tabbed.util.Reflection
import ga.strikepractice.striketab.updater.TabUpdateTask
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

class StrikeTab : JavaPlugin(), CommandExecutor {

    private lateinit var tabManager: TabManager

    override fun onEnable() {
        super.onEnable()
        saveDefaultConfig()
        getCommand("striketab").executor = this
        initializePlugin()
    }

    private fun initializePlugin() {
        // When reloading the config
        Bukkit.getScheduler().cancelTasks(this)
        HandlerList.unregisterAll(this)

        tabManager = TabManager(this)
        tabManager.loadLayouts()
        val ticks = config.getLong("tablist.update-ticks")
        TabUpdateTask(tabManager).runTaskTimerAsynchronously(this, ticks, ticks)
        Bukkit.getOnlinePlayers().forEach { player ->
            tabManager.updateTablist(player)
        }
    }


    private fun reloadPluginConfig() {
        Bukkit.getLogger().info("Reloading config...")
        reloadConfig()
        initializePlugin()
        Bukkit.getLogger().info("Plugin config reloaded successfully.")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val admin = sender.hasPermission("striketab.admin")
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
        }
        sender.sendMessage("${PREFIX}${ChatColor.GOLD}StrikeTab ${description.version} - Tablist for StrikePractice")
        if (admin) {
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab reload${ChatColor.GRAY} - reload the config")
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab debug${ChatColor.GRAY} - toggle debug logging")
        }
        return true
    }

}

/**
 * Translate Bukkit ChatColors
 */
fun String.translateColors(): String = ChatColor.translateAlternateColorCodes('&', this)

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