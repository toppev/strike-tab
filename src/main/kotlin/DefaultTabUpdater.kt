package ga.strikepractice.striketab

import com.keenant.tabbed.Tabbed
import com.keenant.tabbed.item.TextTabItem
import com.keenant.tabbed.tablist.TableTabList
import com.keenant.tabbed.util.Skin
import com.keenant.tabbed.util.Skins
import ga.strikepractice.striketab.util.getCitizensPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.*
import java.util.concurrent.ConcurrentHashMap


val SP_DEFAULT_SKIN: Skin = Skins.DEFAULT_SKIN

class DefaultTabUpdater : TabUpdater, Listener {

    private lateinit var tabbed: Tabbed
    private lateinit var plugin: StrikeTab
    private val tabs = ConcurrentHashMap<UUID, TabData>()
    private val failedSkinLoad = false

    override fun onEnable(plugin: StrikeTab) {
        this.plugin = plugin
        tabbed = Tabbed(plugin)
    }


    override fun updateTab(player: Player, layout: TabManager.TabLayout, bypassTimeLimit: Boolean) {
        val tabData = tabs[player.uniqueId]
        // comparing to previous layout is a very good performance improvement
        if (tabData?.tablist == null || tabData.previousLayout == layout) return
        if (bypassTimeLimit || tabData.lastUpdated + 500 < System.currentTimeMillis()) {
            val tab = tabData.tablist
            layout.slots.forEachIndexed { index, slot ->
                tab.set(index, TextTabItem(slot.text, slot.ping, getSkin(slot.skin)))
            }
            if (tab.footer != layout.footer) tab.footer = layout.footer
            if (tab.header != layout.header) tab.header = layout.header
            tab.batchUpdate()
            tabData.previousLayout = layout
            tabData.lastUpdated = System.currentTimeMillis()
            if (DEBUG) {
                Bukkit.getLogger().info("(Batch)updated ${player.name}'s tab slots.")
            }
        }
    }

    private fun getSkin(name: String?): Skin {
        if (!failedSkinLoad && !name.isNullOrBlank()) {
            try {
                val op = Bukkit.getPlayerExact(name) ?: getCitizensPlayer(name)
                if (op != null) {
                    return Skins.getPlayer(op)
                }
                @Suppress("DEPRECATION")
                val of = Bukkit.getOfflinePlayer(name)
                if (of.hasPlayedBefore()) {
                    return Skins.getPlayer(of.uniqueId)
                }
                if(DEBUG) {
                    Bukkit.getLogger().info("No skin found for $name. Defaulting to default skin.")
                }
            } catch (e: Exception) {
                Bukkit.getLogger().info("Failed to load skin '${name}'. This error will not be logged anymore.")
                e.printStackTrace()
            }
        }
        return SP_DEFAULT_SKIN
    }

    override fun onJoin(player: Player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            val tab = tabbed.newTableTabList(player, plugin.config.getInt("tablist.columns"))
            tabs[player.uniqueId] = TabData(tab)
            tab.isBatchEnabled = true
            if (DEBUG) {
                Bukkit.getLogger().info("Created tablist for ${player.name} (total ${tabs.size} tablists)")
            }
        }
    }

    override fun onLeave(player: Player) {
        tabs.remove(player.uniqueId)
        if (DEBUG) {
            Bukkit.getLogger().info("Removed ${player.name}'s tablist (total ${tabs.size} tablists)")
        }
    }


    class TabData(
        val tablist: TableTabList,
        var previousLayout: TabManager.TabLayout? = null,
        var lastUpdated: Long = 0
    )

}