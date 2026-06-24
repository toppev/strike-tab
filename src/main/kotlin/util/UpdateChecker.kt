package ga.strikepractice.striketab.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ga.strikepractice.striketab.DEBUG
import ga.strikepractice.striketab.PREFIX
import ga.strikepractice.striketab.StrikeTab
import ga.strikepractice.striketab.translateColors
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.lang.reflect.Type
import java.net.URL
import java.util.concurrent.TimeUnit.HOURS

private const val UPDATE_URL = "https://toppe.dev/striketab/version.json"
private val MAP_TYPE: Type = object : TypeToken<Map<String?, String?>>() {}.type

/**
 * Compares two dot-separated version strings numerically (e.g. 0.3.9 < 0.3.10).
 * Non-numeric or missing parts are treated as 0.
 */
val VERSION_COMPARATOR = Comparator<String> { a, b ->
    val aParts = a.split(".")
    val bParts = b.split(".")
    for (i in 0 until maxOf(aParts.size, bParts.size)) {
        val cmp = (aParts.getOrNull(i)?.toIntOrNull() ?: 0)
            .compareTo(bParts.getOrNull(i)?.toIntOrNull() ?: 0)
        if (cmp != 0) return@Comparator cmp
    }
    0
}

class UpdateChecker(private val plugin: StrikeTab) : Listener {

    val joinMessages = mutableSetOf<String>()
    var lastChecked: Long = 0

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, {
            try {
                checkForUpdates()
            } catch (e: Exception) {
                Bukkit.getLogger().warning("StrikeTab failed to check for updates.")
                if (DEBUG) e.printStackTrace()
            }
        }, 0, HOURS.toSeconds(12) * 20)
    }

    /**
     * Checks for updates.
     * WARNING: this is blocking!!
     */
    fun checkForUpdates() {
        Bukkit.getLogger().info("Checking for StrikeTab updates...")
        val content = URL(UPDATE_URL + "?ver=" + plugin.description.version).readText()
        val json: Map<String, String> = Gson().fromJson(content, MAP_TYPE)
        val latestVer = json["version"] ?: throw Exception("No 'version' in response: $content")
        val currentVer = plugin.description.version
        if (VERSION_COMPARATOR.compare(latestVer, currentVer) > 0) {
            joinMessages.clear()
            joinMessages.apply {
                add("There's a new update available: https://github.com/toppev/strike-tab/releases")
                add("You're on $currentVer and the latest version is $latestVer.")
                // Only show changelog entries for versions newer than the current one
                json.filter { it.key.startsWith("message-v") }
                    .filter { VERSION_COMPARATOR.compare(it.key.substringAfter("message-v"), currentVer) > 0 }
                    .entries
                    .sortedWith(compareBy(VERSION_COMPARATOR) { it.key.substringAfter("message-v") })
                    .forEach {
                        val ver = it.key.substringAfter("message-")
                        add("$PREFIX$ver: ${it.value.translateColors()}")
                    }
                forEach { Bukkit.getLogger().info(ChatColor.stripColor(it)) }
            }
        }
        lastChecked = System.currentTimeMillis()
    }


    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val p = event.player
        if (p.isOp || p.hasPermission("striketab.admin")) {
            joinMessages.forEach { p.sendMessage(it) }
        }
    }

}
