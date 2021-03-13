package ga.strikepractice.striketab.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ga.strikepractice.striketab.DEBUG
import ga.strikepractice.striketab.PREFIX
import ga.strikepractice.striketab.StrikeTab
import ga.strikepractice.striketab.translateColors
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.lang.reflect.Type
import java.net.URL
import java.util.concurrent.TimeUnit.HOURS

const val UPDATE_URL = "https://toppe.dev/striketab/version.json"
val MAP_TYPE: Type = object : TypeToken<Map<String?, String?>>() {}.type

class UpdateChecker(val plugin: StrikeTab) : Listener {

    val joinMessages = mutableSetOf<String>()

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

    fun checkForUpdates() {
        val content = URL(UPDATE_URL + "?ver=" + plugin.description.version).readText()
        val json: Map<String, String> = Gson().fromJson(content, MAP_TYPE)
        val latestVer = json["version"] ?: throw Exception("No 'version' in response: $content")
        val currentVer = plugin.description.version
        if (latestVer > currentVer) {
            joinMessages.add("${PREFIX}There's a new update available: https://github.com/toppev/strike-tab/releases")
            joinMessages.add("${PREFIX}You're on $currentVer and the latest version is $latestVer.")
            (json["message-$currentVer"] ?: json["message"])?.let {
                joinMessages.add(PREFIX + it.translateColors())
            }
            joinMessages.forEach { Bukkit.getLogger().info(it) }
        }
    }


    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val p = event.player
        if (p.isOp || p.hasPermission("striketab.admin")) {
            joinMessages.forEach { p.sendMessage(it) }
        }
    }

}