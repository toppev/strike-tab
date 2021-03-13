package ga.strikepractice.striketab.updater

import ga.strikepractice.striketab.TabManager
import ga.strikepractice.striketab.debug
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class TabUpdateTask(private val tabManager: TabManager) : BukkitRunnable() {

    override fun run() {
        val st = System.currentTimeMillis()
        debug { "Updating tablists of ${Bukkit.getOnlinePlayers().size} players" }
        Bukkit.getOnlinePlayers().forEach { player ->
            try {
                tabManager.updateTablist(player)
            } catch (e: Exception) {
                Bukkit.getLogger().warning("Failed to update tablist for ${player.name}")
                e.printStackTrace()
            }
        }
        debug { "Updated tablists in ${System.currentTimeMillis() - st} ms." }

    }

}