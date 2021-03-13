package ga.strikepractice.striketab.util

import com.comphenix.protocol.ProtocolLibrary
import ga.strikepractice.striketab.DEBUG
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import us.myles.ViaVersion.api.Via

private const val LEGACY_SUPPORT = true

fun isSupportedClient(player: Player) = LEGACY_SUPPORT || !isLegacyClient(player)

fun isLegacyClient(player: Player): Boolean {
    val viaVer = getViaVersionVersion(player)
    val ver = ProtocolLibrary.getProtocolManager().getProtocolVersion(player)
    if (DEBUG) Bukkit.getLogger().info("player ${player.name} ProtocolLib version: $ver, ViaVersion version: $viaVer")
    return viaVer == 5 || ver < 47
}


private fun getViaVersionVersion(player: Player): Int? {
    return try {
        Via.getAPI().getPlayerVersion(player)
    } catch (e: Exception) {
        if (DEBUG) Bukkit.getLogger().warning("Failed to get viaversion client version")
        null
    }
}