package ga.strikepractice.striketab

import org.bukkit.entity.Player

interface TabUpdater {

    fun onEnable(plugin: StrikeTab) {}

    fun updateTab(player: Player, tabLayout: TabManager.TabLayout, bypassTimeLimit: Boolean = false)

    fun onJoin(player: Player) {}

    fun onLeave(player: Player) {}

}