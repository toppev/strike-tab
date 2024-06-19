package ga.strikepractice.striketab.util

import com.comphenix.protocol.ProtocolLibrary
import ga.strikepractice.striketab.PREFIX
import ga.strikepractice.striketab.debug
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import com.viaversion.viaversion.api.Via

var LEGACY_SUPPORT = true

val VIA_REWIND_SUPPORT: Boolean by lazy {
    (Bukkit.getPluginManager().getPlugin("ViaRewind")?.isEnabled ?: false).also {
        if (it) Bukkit.getLogger().info("${PREFIX}ViaRewind support detected")
    }
}

fun isSupportedClient(player: Player) = LEGACY_SUPPORT || !isLegacyClient(player)

fun isLegacyClient(player: Player): Boolean {
    val viaVer = getViaVersionVersion(player)
    val ver = ProtocolLibrary.getProtocolManager().getProtocolVersion(player)
    return viaVer == 5 || ver < 47
}

private fun getViaVersionVersion(player: Player): Int? {
    if (!VIA_REWIND_SUPPORT) return null
    return try {
        Via.getAPI().getPlayerVersion(player)
    } catch (e: Exception) {
        debug { "Failed to get viaversion client version" }
        null
    }
}
