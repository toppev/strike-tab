package ga.strikepractice.striketab.updater

import ga.strikepractice.striketab.StrikeTab
import ga.strikepractice.striketab.TabLayout
import org.bukkit.entity.Player

interface TabUpdater {

    fun onEnable(plugin: StrikeTab) { /* optional */ }

    fun updateTab(player: Player, layout: TabLayout, bypassTimeLimit: Boolean = false)

    fun onJoin(player: Player) { /* optional */ }

    fun onLeave(player: Player) { /* optional */ }

}