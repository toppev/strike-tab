package ga.strikepractice.striketab.util

import com.comphenix.protocol.ProtocolLibrary
import ga.strikepractice.striketab.DEBUG
import org.bukkit.Bukkit
import org.bukkit.entity.Player


fun isSupportedClient(player: Player): Boolean {
    val ver = ProtocolLibrary.getProtocolManager().getProtocolVersion(player)
    return ver >= 47
}