package ga.strikepractice.striketab

import ga.strikepractice.striketab.bstats.CustomCharts
import ga.strikepractice.striketab.updater.TabUpdateTask
import ga.strikepractice.striketab.util.LEGACY_SUPPORT
import ga.strikepractice.striketab.util.UpdateChecker
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

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
        try {
            val metrics = Metrics(this, 11120)
            CustomCharts.getCustomCharts(this).forEach { metrics.addCustomChart(it) }
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to start bStats metrics...")
            e.printStackTrace()
        }
    }

    override fun onDisable() {
        tabManager.updater.onDisable(this)
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
                CompletableFuture.runAsync {
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
            if (args[0].equals("skins", true)) {
                sender.sendMessage("${PREFIX}Currently supported custom skins (additionally to online and offline player skins):")
                val str = tabManager.updater.supportedSkins().sorted().joinToString(separator = ", ")
                sender.sendMessage(ChatColor.GRAY.toString() + str)
                return true
            }
        }
        sender.sendMessage("${PREFIX}${ChatColor.GOLD}StrikeTab ${description.version} - ${this.description.description}")
        if (admin) {
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab reload${ChatColor.GRAY} - reload the config")
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab debug${ChatColor.GRAY} - toggle debug logging")
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab update${ChatColor.GRAY} - check for updates")
            sender.sendMessage("${PREFIX}${ChatColor.YELLOW}/striketab skins${ChatColor.GRAY} - currently supported skins")
            if (System.currentTimeMillis() - updateChecker.lastChecked > TimeUnit.MINUTES.toMillis(10)) {
                CompletableFuture.runAsync { updateChecker.checkForUpdates() }
            }
        }
        return true
    }

}

/**
 * Translate Bukkit ChatColors
 */
val hexPattern = Regex("#[a-fA-F\\d]{6}")

fun String.translateColors(): String {
    var translated = this
    hexPattern.findAll(this).forEach { matchResult ->
        val result = matchResult.value
        val ch = result.replace('#', 'x').toCharArray()
        val builder = StringBuilder()
        for (c in ch) {
            builder.append("§").append(c)
        }
        translated = translated.replace(result, builder.toString())
    }
    return ChatColor.translateAlternateColorCodes('&', translated)
}

inline fun debug(message: () -> String) {
    if (DEBUG) {
        Bukkit.getLogger().info(DEBUG_PREFIX + message.invoke())
    }
}
