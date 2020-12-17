package ga.strikepractice.striketab

import com.keenant.tabbed.item.PlayerTabItem
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class Placeholders() {

    private var enabled = true

    init {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            enabled = false
            val errMsg = "${ChatColor.RED} PlaceholderAPI is not installed! StrikeTab requires it."
            Bukkit.getLogger().warning(errMsg)
            Bukkit.getOnlinePlayers().filter { it.hasPermission("striketab.admin") }.forEach { it.sendMessage(errMsg) }
        }
    }

    fun handlePlaceHolders(p: Player?, text: String?): String {
        // "" (not " ") is replaced with a player
        if (!enabled || text == null) return " "
        val str = PlaceholderAPI.setPlaceholders(p, text)
        if (str.contains("[display=false]") || str.contains("[display=!true]")) return " "
        return str
    }
}