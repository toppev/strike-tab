package ga.strikepractice.striketab.updater

import ga.strikepractice.striketab.DEBUG
import ga.strikepractice.striketab.TabManager
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class TabUpdateTask(private val tabManager: TabManager) : BukkitRunnable() {

    override fun run() {
        val st = System.currentTimeMillis()
        if (DEBUG) {
            Bukkit.getLogger().info("Updating tablists of ${Bukkit.getOnlinePlayers().size} players")
        }
        Bukkit.getOnlinePlayers().forEach { player ->
            try {
                tabManager.updateTablist(player)
            } catch (e: Exception) {
                Bukkit.getLogger().warning("Failed to update tablist for ${player.name}")
                e.printStackTrace()
            }
        }
        if (DEBUG) {
            Bukkit.getLogger().info("Updated tablists in ${System.currentTimeMillis() - st} ms.")
        }

    }

}