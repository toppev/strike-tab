package ga.strikepractice.striketab.updater

import ga.strikepractice.striketab.StrikeTab
import ga.strikepractice.striketab.TabLayout
import org.bukkit.entity.Player

interface TabUpdater {

    fun onEnable(plugin: StrikeTab) { /* optional */
    }

    fun updateTab(player: Player, layout: TabLayout, bypassTimeLimit: Boolean = false)

    /** Also called on current online players when the plugin enables. */
    fun onJoin(player: Player) { /* optional */
    }

    fun onLeave(player: Player) { /* optional */
    }

    fun supportedSkins(): Iterable<String>

}