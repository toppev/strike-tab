package ga.strikepractice.striketab.util

import com.comphenix.protocol.ProtocolLibrary
import ga.strikepractice.striketab.PREFIX
import ga.strikepractice.striketab.debug
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import us.myles.ViaVersion.api.Via

var LEGACY_SUPPORT = true

private val viaVersionSupport: Boolean by lazy {
    (Bukkit.getPluginManager().getPlugin("ViaRewind")?.isEnabled ?: false).also {
        if (it) Bukkit.getLogger().info("${PREFIX}ViaRewind support detected")
    }
}

fun isSupportedClient(player: Player) = LEGACY_SUPPORT || !isLegacyClient(player)

fun isLegacyClient(player: Player): Boolean {
    val viaVer = getViaVersionVersion(player)
    val ver = ProtocolLibrary.getProtocolManager().getProtocolVersion(player)
    debug { "player ${player.name} ProtocolLib version: $ver, ViaVersion version: $viaVer" }
    return viaVer == 5 || ver < 47
}


private fun getViaVersionVersion(player: Player): Int? {
    if (!viaVersionSupport) return null
    return try {
        Via.getAPI().getPlayerVersion(player)
    } catch (e: Exception) {
        debug { "Failed to get viaversion client version" }
        null
    }
}