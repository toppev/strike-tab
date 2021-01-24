package ga.strikepractice.striketab.util

import com.comphenix.protocol.ProtocolLibrary
import ga.strikepractice.striketab.DEBUG
import org.bukkit.Bukkit
import org.bukkit.entity.Player


fun isSupportedClient(player: Player): Boolean {
    val ver = ProtocolLibrary.getProtocolManager().getProtocolVersion(player)
    if (DEBUG) Bukkit.getLogger().info("${player.name}'s protocol version is $ver")
    return ver >= 47
}