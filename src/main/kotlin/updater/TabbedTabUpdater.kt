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
import ga.strikepractice.striketab.debug
import ga.strikepractice.striketab.layout.TabLayout
import ga.strikepractice.striketab.util.getCitizensPlayer
import ga.strikepractice.striketab.util.isLegacyClient
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scoreboard.Team
import java.util.*
import java.util.concurrent.ConcurrentHashMap


val DEFAULT_SKIN: Skin = Skins.DEFAULT_SKIN

class TabbedTabUpdater : TabUpdater, Listener {

    private var spreadCounter = 0L

    private lateinit var tabbed: Tabbed
    private lateinit var plugin: StrikeTab
    private val tabs = ConcurrentHashMap<UUID, TabData>()

    private val legacyNameProvider = object : SimpleTabList.NameProvider {
        override fun getName(index: Int) = blankLines[index]
    }
    private val blankLines: List<String> by lazy {
        (1..4).flatMap { index ->
            ChatColor.values().map {
                it.toString().repeat(index) + ChatColor.RESET
            }
        }.distinct()
    }

    private val skinMapping = mapOf<String, Skin>(
        "COBBLE" to Skins.BLOCK_COBBLE,
        "LOG" to Skins.BLOCK_LOG,
        "AQUARIUM" to Skins.DECOR_AQUARIUM,
        "CLOCK" to Skins.DECOR_CLOCK,
        "DOUGHNUT" to Skins.DECOR_DOUGHNUT,
        "PRESENT" to Skins.DECOR_PRESENT,
        "RADIO" to Skins.DECOR_RADIO,
        "SKULL" to Skins.DECOR_SKULL,
        "TV" to Skins.DECOR_TV,
    ) + ChatColor.values().take(16).map { "DOT_${it.name}" to Skins.getDot(it) }


    override fun onEnable(plugin: StrikeTab) {
        this.plugin = plugin
        tabbed = Tabbed(plugin)
    }

    override fun updateTab(player: Player, layout: TabLayout, bypassTimeLimit: Boolean) {
        val tabData = tabs[player.uniqueId]
        // comparing to previous layout is a very good performance improvement
        if (tabData?.tablist == null || tabData.previousLayout == layout) return
        if (bypassTimeLimit || tabData.lastUpdated + 500 < System.currentTimeMillis()) {
            val legacy = isLegacyClient(player)
            // We want to update only once for legacy clients, otherwise the tab may be in wrong order because of changes
            // Thus, ping etc are not updated for legacy clients which is ok
            if (!legacy || !tabData.legacyInit) {
                val tab = tabData.tablist
                layout.slots.forEachIndexed { index, slot ->
                    tab.set(index, TextTabItem(slot.text, slot.ping, getSkin(slot.skin)))
                }
                if (layout.footer != null && tab.footer != layout.footer) tab.footer = layout.footer
                if (layout.header != null && tab.header != layout.header) tab.header = layout.header
                tab.batchUpdate()
                tabData.legacyInit = true
            }
            if (legacy) {
                handleLegacyClient(player, layout)
            }
            tabData.previousLayout = layout
            tabData.lastUpdated = System.currentTimeMillis()
            debug { "(Batch)updated ${player.name}'s tab slots." }
        }
    }

    // Kind of a temp fix for 1.7 clients (still testing)
    private fun handleLegacyClient(player: Player, layout: TabLayout) {
        // can't really change teams asynchronously
        Bukkit.getScheduler().runTaskLater(plugin, {
            val st = if (DEBUG) System.currentTimeMillis() else 0
            if (player.scoreboard == Bukkit.getScoreboardManager().mainScoreboard) {
                player.scoreboard = Bukkit.getScoreboardManager().newScoreboard
            }
            val board = player.scoreboard
            layout.slots.forEachIndexed { index, slot ->
                if (index > 59) {
                    debug { "Blocked main thread for ~${System.currentTimeMillis() - st}ms while updating ${player.name}'s legacy teams" }
                    return@runTaskLater
                }
                // 1.7 tab starts ordering top row first, 1.8+ does 1. column first
                // converts 1.8 -> 1.7 slots
                var legacyIndex = 3 * ((index % 20) + 1) - ((59 - index) / 20 + 1)
                // FIXME: doesn't work correctly
                // for some weird reason index=24 does not show at all?
                // if we don't do this "skip" it the tab will be one slot offset
                if (legacyIndex > 24) legacyIndex++
                val teamName = "striketab-$legacyIndex"
                val team = board.getTeam(teamName) ?: (board.registerNewTeam(teamName).also {
                    it.addEntry(legacyNameProvider.getName(legacyIndex))
                })
                updateLegacyTeam(team, ChatColor.RESET.toString() + slot.text)
            }
        }, spreadCounter++ % 10) // spread updates across multiple ticks to avoid lag spikes
    }

    private fun updateLegacyTeam(team: Team, text: String) {
        if (text.length <= 16) {
            team.prefix = text
            team.suffix = ""
        } else {
            team.prefix = text.substring(0, 16)
            val suffix = ChatColor.getLastColors(team.prefix) + text.substring(16)
            team.suffix = suffix.take(16)
        }
    }

    private fun getSkin(name: String?): Skin {
        if (!name.isNullOrBlank()) {
            try {
                val builtIn = skinMapping[name]
                if (builtIn != null) {
                    return builtIn
                }
                val playerSkin = Bukkit.getPlayerExact(name) ?: getCitizensPlayer(name)
                if (playerSkin != null) {
                    return Skins.getPlayer(playerSkin)
                }
                @Suppress("DEPRECATION")
                val of = Bukkit.getOfflinePlayer(name)
                return Skins.getPlayer(of.uniqueId)
            } catch (e: Exception) {
                Bukkit.getLogger().info("Failed to load skin '${name}'.")
                e.printStackTrace()
            }
        }
        return DEFAULT_SKIN
    }

    override fun onJoin(player: Player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, {
            val tab = tabbed.newTableTabList(player, plugin.config.getInt("tablist.columns"))
            if (isLegacyClient(player)) {
                tab.isLegacyTab = true
                tab.setNameProvider(legacyNameProvider)
                clearOnlinePlayers(player)
            }
            tab.enable()
            tabs[player.uniqueId] = TabData(tab)
            tab.isBatchEnabled = true

            debug { "Created tablist for ${player.name} (total ${tabs.size} tablists)" }

            plugin.tabManager.updateTablist(player)
        }, 15)
    }

    // For 1.7 clients
    private fun clearOnlinePlayers(player: Player) {
        val names = Bukkit.getOnlinePlayers().map {
            PlayerInfoData(
                WrappedGameProfile.fromPlayer(it),
                1,
                NativeGameMode.SURVIVAL,
                WrappedChatComponent.fromText(it.name)
            )
        }
        Packets.send(player, listOf(Packets.getPacket(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER, names)))
    }

    override fun onLeave(player: Player) {
        tabs.remove(player.uniqueId)
        debug { "Removed ${player.name}'s tablist (total ${tabs.size} tablists)" }

        player.scoreboard.teams.forEach {
            if (it.name.contains("striketab")) {
                it.unregister()
            }
        }

    }

    override fun supportedSkins(): Iterable<String> = skinMapping.keys

    class TabData(
        val tablist: TableTabList,
        var previousLayout: TabLayout? = null,
        var lastUpdated: Long = 0,
        var legacyInit: Boolean = false
    ) {
    }


}