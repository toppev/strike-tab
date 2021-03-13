package ga.strikepractice.striketab.util

import ga.strikepractice.striketab.debug
import net.citizensnpcs.api.CitizensAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val citizensSupport: Boolean by lazy {
    Bukkit.getPluginManager().getPlugin("Citizens")?.isEnabled == true
        .also { debug { "Citizen found: $it" } }
}

fun getCitizensPlayer(name: String): Player? {
    if (citizensSupport) {
        for (reg in CitizensAPI.getNPCRegistries()) {
            for (npc in reg) {
                if (npc.entity is Player && npc.name == name) {
                    return npc.entity as Player
                }
            }
        }
    }
    return null
}