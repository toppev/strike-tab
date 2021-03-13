package ga.strikepractice.striketab.updater

import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.keenant.tabbed.Tabbed
import com.keenant.tabbed.item.TextTabItem
import com.keenant.tabbed.tablist.SimpleTabList
import com.keenant.tabbed.tablist.TableTabList
import com.keenant.tabbed.util.Packets
import com.keenant.tabbed.util.Skin
import com.keenant.tabbed.util.Skins
import ga.strikepractice.striketab.DEBUG
import ga.strikepractice.striketab.StrikeTab
import ga.strikepractice.striketab.TabLayout
import ga.strikepractice.striketab.TabSlot
import ga.strikepractice.striketab.util.getCitizensPlayer
import ga.strikepractice.striketab.util.isLegacyClient
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scoreboard.Team
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import org.bukkit.WorldCreator.name


val SP_DEFAULT_SKIN: Skin = Skins.DEFAULT_SKIN

class TabbedTabUpdater : TabUpdater, Listener {

    private lateinit var tabbed: Tabbed
    private lateinit var plugin: StrikeTab
    private val tabs = ConcurrentHashMap<UUID, TabData>()

    private val nameProvider = object : SimpleTabList.NameProvider {
        override fun getName(index: Int) = blankLines[index]
    }
    private val blankLines: List<String> by lazy {
        (1..4).flatMap { index ->
            ChatColor.values().map {
                it.toString().repeat(index) + ChatColor.RESET
            }
        }
    }


    override fun onEnable(plugin: StrikeTab) {
        this.plugin = plugin
        tabbed = Tabbed(plugin)
        SimpleTabList.setNameProvider(nameProvider)
    }


    override fun updateTab(player: Player, layout: TabLayout, bypassTimeLimit: Boolean) {
        val tabData = tabs[player.uniqueId]
        // comparing to previous layout is a very good performance improvement
        if (tabData?.tablist == null || tabData.previousLayout == layout) return
        if (bypassTimeLimit || tabData.lastUpdated + 500 < System.currentTimeMillis()) {
            val tab = tabData.tablist
            var counter = 0
            layout.slots.forEachIndexed { index, slot ->
                tab.set(index, TextTabItem(slot.text, slot.ping, getSkin(slot.skin)))
            }
            if (layout.footer != null && tab.footer != layout.footer) tab.footer = layout.footer
            if (layout.header != null && tab.header != layout.header) tab.header = layout.header
            tab.batchUpdate()
            tabData.previousLayout = layout
            tabData.lastUpdated = System.currentTimeMillis()
            if (isLegacyClient(player)) handleLegacyClient(player, layout)
            if (DEBUG) {
                Bukkit.getLogger().info("(Batch)updated ${player.name}'s tab slots.")
            }
        }
    }

    // Kind of a temp fix for 1.7 clients (still testing)
    // TODO: better performance?
    private fun handleLegacyClient(player: Player, layout: TabLayout) {
        // can't really change teams asynchronously
        Bukkit.getScheduler().runTask(plugin) {
            val board = player.scoreboard
            layout.slots.forEachIndexed { index, slot ->
                if (index >= 59) return@runTask
                // refactor?
                val legacyIndex = when {
                    index < 20 -> 3 * (index + 1) - 3
                    index < 40 -> 3 * ((index % 20) + 1) - 2
                    else -> 3 * ((index % 20) + 1) - 1
                }
                if(legacyIndex < 0) println(legacyIndex.toString() + " from " + index)

                val teamName = "striketab-$legacyIndex"
                val team = board.getTeam(teamName) ?: board.registerNewTeam(teamName)
                val nmsName = nameProvider.getName(legacyIndex)
                if (!team.hasEntry(nmsName)) team.addEntry(nmsName)
                updateLegacyTeam(team, slot.text)
            }
        }
    }

    private fun updateLegacyTeam(team: Team, text: String) {
        if (text.length > 16) {
            team.prefix = text.substring(0, 16)
            var suffix = ChatColor.getLastColors(team.prefix) + text.substring(16)
            if (suffix.length > 16) {
                if (suffix.length <= 16) {
                    suffix = text.substring(16)
                    team.suffix = suffix
                } else {
                    team.suffix = suffix.substring(0, 16)
                }
            } else {
                team.suffix = suffix
            }
        } else {
            team.prefix = text
            team.suffix = ""
        }
    }

    private fun getSkin(name: String?): Skin {
        if (!name.isNullOrBlank()) {
            try {
                val op = Bukkit.getPlayerExact(name) ?: getCitizensPlayer(name)
                if (op != null) {
                    return Skins.getPlayer(op)
                }
                @Suppress("DEPRECATION")
                val of = Bukkit.getOfflinePlayer(name)
                return Skins.getPlayer(of.uniqueId)
            } catch (e: Exception) {
                Bukkit.getLogger().info("Failed to load skin '${name}'.")
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
            clearOnlinePlayers(player)
            if (DEBUG) {
                Bukkit.getLogger().info("Created tablist for ${player.name} (total ${tabs.size} tablists)")
            }
        }
    }

    // For 1.7 clients
    private fun clearOnlinePlayers(player: Player) {
        val names = Bukkit.getOnlinePlayers().map {
            PlayerInfoData(
                WrappedGameProfile.fromPlayer(it),
                0,
                NativeGameMode.SURVIVAL,
                WrappedChatComponent.fromText(it.name)
            )
        }
        Packets.send(player, listOf(Packets.getPacket(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER, names)))
    }

    override fun onLeave(player: Player) {
        tabs.remove(player.uniqueId)
        if (DEBUG) {
            Bukkit.getLogger().info("Removed ${player.name}'s tablist (total ${tabs.size} tablists)")
        }
    }

    class TabData(
        val tablist: TableTabList,
        var previousLayout: TabLayout? = null,
        var lastUpdated: Long = 0,
    )


}