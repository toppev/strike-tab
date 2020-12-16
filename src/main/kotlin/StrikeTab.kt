package ga.strikepractice.striketab

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

var DEBUG = false
val PREFIX = "${ChatColor.GRAY}[${ChatColor.GREEN}StrikeTab${ChatColor.GRAY}] "

class StrikeTab : JavaPlugin(), CommandExecutor {

    private lateinit var tabManager: TabManager

    override fun onEnable() {
        super.onEnable()
        tabManager = TabManager(this)
        saveDefaultConfig()
        initializeTabList()
        val ticks = config.getLong("tablist.update-ticks", 20)
        TabUpdateTask(tabManager).runTaskTimerAsynchronously(this, ticks, ticks)
        getCommand("striketab").executor = this
    }

    private fun initializeTabList() {
        tabManager.loadLayouts()
        Bukkit.getOnlinePlayers().forEach { player ->
            tabManager.setTablist(player)
        }
    }


    private fun reloadPluginConfig() {
        Bukkit.getLogger().info("Reloading config...")
        reloadConfig()
        initializeTabList()
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
                sender.sendMessage("${ChatColor.GREEN} Config reloaded!")
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
            sender.sendMessage("${PREFIX}/striketab reload - reload the config")
            sender.sendMessage("${PREFIX}/striketab debug - toggle debug logging (decreases performance slightly)")
        }
        return true
    }


}