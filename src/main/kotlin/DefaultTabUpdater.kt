package ga.strikepractice.striketab

import com.keenant.tabbed.Tabbed
import com.keenant.tabbed.item.TextTabItem
import com.keenant.tabbed.tablist.TableTabList
import com.keenant.tabbed.util.Skin
import com.keenant.tabbed.util.Skins
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentHashMap


val SP_DEFAULT_SKIN: Skin = Skins.DEFAULT_SKIN

class DefaultTabUpdater : TabUpdater, Listener {

    private lateinit var tabbed: Tabbed
    private val tabs = ConcurrentHashMap<Player, TabData>()
    private lateinit var plugin: StrikeTab

    override fun onEnable(plugin: StrikeTab) {
        this.plugin = plugin
        tabbed = Tabbed(plugin)
    }


    override fun updateTab(player: Player, tabLayout: TabManager.TabLayout) {
        val tabdata = tabs[player]
        if (tabdata?.tablist != null) {
            val tab = tabdata.tablist
            // Only update if it needs update (i.e. old layout is different than the new one)
            // Reduces per player tab update from ~25-50 ms to ~1-3 ms on my **** test server
            if (tabLayout != tabdata.previousLayout) {
                tabLayout.slots.forEachIndexed { index, slot ->
                    val skin = if (slot.skin != null) Skins.getPlayer(slot.skin) else SP_DEFAULT_SKIN
                    tab.set(index, TextTabItem(slot.text, slot.ping, skin))
                }
                tab.batchUpdate()
                if (DEBUG) {
                    Bukkit.getLogger().info("(Batch)updated $player's tablist with ${tabLayout.slots.size} slots.")
                }
            }
            tabdata.previousLayout = tabLayout
        }
    }

    override fun onJoin(player: Player) {
        object : BukkitRunnable() {
            override fun run() {
                val tab = tabbed.newTableTabList(player)
                tabs[player] = TabData(tab)
                tab.isBatchEnabled = true
                if (DEBUG) {
                    Bukkit.getLogger().info("Created tablist for ${player.name} (total ${tabs.size} tablists)")
                }
            }
        }.runTaskAsynchronously(plugin)
    }

    override fun onLeave(player: Player) {
        tabs.remove(player)
        if (DEBUG) {
            Bukkit.getLogger().info("Removed ${player.name}'s tablist (total ${tabs.size} tablists)")
        }
    }


    class TabData(
        val tablist: TableTabList,
        var previousLayout: TabManager.TabLayout? = null
    )

}