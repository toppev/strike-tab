package ga.strikepractice.striketab.util

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player

class Placeholders {

    fun handlePlaceHolders(p: Player?, text: String?): String {
        val str = PlaceholderAPI.setPlaceholders(p, text)
        // "" (not " ") is replaced with a player
        if (str.contains("[display=false]") || str.contains("[display=!true]")) return " "
        return str
    }

}