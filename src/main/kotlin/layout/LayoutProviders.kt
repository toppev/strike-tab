package ga.strikepractice.striketab.layout

import ga.strikepractice.StrikePractice
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/*
 * If we ever want to support other plugins/APIs we can just add them here in PROVIDER
 */

val PROVIDER: LayoutProvider by lazy {
    // Take first supported provider
    listOf(
        StrikePracticeLayout()
    ).firstOrNull { it.isSupported() }?.also {
        Bukkit.getLogger().info("Using ${it.javaClass.simpleName} layout provider.")
    } ?: DefaultLayoutProvider().also {
        Bukkit.getLogger().info("No supported tab layout providers found. Defaulting to the default layout.")
    }
}

fun getLayout(player: Player): TabLayoutType = PROVIDER.getLayout(player)

interface LayoutProvider {
    fun isSupported(): Boolean

    fun getLayout(player: Player): TabLayoutType
}


// Built-in providers

private class DefaultLayoutProvider : LayoutProvider {
    override fun isSupported() = true
    override fun getLayout(player: Player) = TabLayoutType.DEFAULT
}

private class StrikePracticeLayout : LayoutProvider {

    override fun isSupported() = Bukkit.getPluginManager().getPlugin("StrikePractice")?.isEnabled == true

    override fun getLayout(player: Player): TabLayoutType {
        val api = StrikePractice.getAPI()
        return when {
            api.isInFight(player) -> TabLayoutType.IN_MATCH
            // We can add more here
            else -> TabLayoutType.DEFAULT
        }
    }

}