package ga.strikepractice.striketab.util

import com.keenant.tabbed.util.Reflection
import ga.strikepractice.striketab.debug
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.lang.reflect.Method

private lateinit var pingField: Field
private lateinit var pingMethod: Method


fun getPlayerPing(player: Player): Int =
    getLegacyPingField(player) ?: getPingByMethod(player) ?: 0.also { debug { "Failed to get player ping" } }

private fun getLegacyPingField(player: Player): Int? {
    try {
        val craftPlayer = Reflection.getHandle(player)
        if (!::pingField.isInitialized) {
            pingField = craftPlayer.javaClass.getDeclaredField("ping")
        }
        return pingField.getInt(craftPlayer)
    } catch (e: Exception) {
        debug { "getLegacyPing failed ${e.cause} ${e.message}" }
    }
    return null
}

private fun getPingByMethod(player: Player): Int? {
    try {
        if (!::pingMethod.isInitialized) {
            pingMethod = player.javaClass.getDeclaredMethod("getPing")
        }
        return pingMethod.invoke(player) as Int
    } catch (e: Exception) {
        debug { "getPlayerPing failed ${e.cause} ${e.message}" }
    }
    return null
}
