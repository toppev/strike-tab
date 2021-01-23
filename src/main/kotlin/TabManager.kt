package ga.strikepractice.striketab

import ga.strikepractice.striketab.updater.TabUpdater
import ga.strikepractice.striketab.updater.TabbedTabUpdater
import ga.strikepractice.striketab.util.Placeholders
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*

class TabManager(private val plugin: StrikeTab) : Listener {

    private val layouts = EnumMap<TabLayoutType, TabLayout>(TabLayoutType::class.java)

    private val ranksManager = RanksManager(plugin)
    private val updater: TabUpdater = TabbedTabUpdater()
    private val placeholders = Placeholders()
    private val columns = plugin.config.getInt("tablist.columns")
    private val columnSize = when (columns) {
        3 -> 15
        4 -> 20
        else -> 20 // idk, someone can correct this
    }

    init {
        updater.onEnable(plugin)
        Bukkit.getLogger().info("Using ${updater.javaClass.name} for tablist with $columns columns of size $columnSize")
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun parseListOrString(key: String): String {
        return when (val obj = plugin.config.get(key)) {
            is List<*> -> obj.joinToString(separator = "\n")
            else -> obj.toString()
        }
    }

    fun loadLayouts() {
        val header = parseListOrString("header").ifEmpty {
            Bukkit.getLogger().info("StrikeTab header disabled")
            null
        }
        val footer = parseListOrString("footer").ifEmpty {
            Bukkit.getLogger().info("StrikeTab footer disabled")
            null
        }
        TabLayoutType.values().forEach { type ->
            try {
                Bukkit.getLogger().info("Loading tablist layout for $type")
                val slots = mutableListOf<String>()
                for (column in 1..columns) {
                    val path = "slots.${type.toString().toLowerCase()}.column-$column"
                    val temp = plugin.config.getStringList(path).toMutableList()
                    val missing = columnSize - temp.size
                    if (missing > 0) {
                        Bukkit.getLogger().info("Layout at $path is missing $missing slots. Players may be appended.")
                        for (i in 0 until missing) temp.add("") // "" means it can be replaced with a player
                    }
                    if (temp.size > columnSize) {
                        temp.subList(0, columnSize)
                        Bukkit.getLogger()
                            .warning("Tablist at $path has too many slots! ${temp.size} > $columnSize")
                    }
                    slots += temp
                }
                val layout = TabLayout.parse(
                    slots.map { it.translateColors() },
                    header?.translateColors(),
                    footer?.translateColors()
                )
                layouts[type] = layout
                Bukkit.getLogger().info("$type tablist loaded")
                if (DEBUG) {
                    slots.forEach { Bukkit.getLogger().info(it) }
                    Bukkit.getLogger().info(layout.toString())
                }
            } catch (e: Exception) {
                Bukkit.getLogger().warning("Failed to load tablist type $type. An error occurred:")
                e.printStackTrace()
            }
        }
    }

    fun updateTablist(player: Player, layoutType: TabLayoutType = getLayout(player), bypassTimeLimit: Boolean = false) {
        val layout = layouts[layoutType]!!
        val st: Long = if (DEBUG) System.currentTimeMillis() else 0
        var playerIndex = 0
        val personalSlots = layout.slots.map { slot ->
            val text = placeholders.handlePlaceHolders(player, slot.text)
            // Replace "" with a real player
            if (text == "") {
                val realPlayer = ranksManager.getNextPlayer(playerIndex++)
                if (realPlayer != null) {
                    return@map slot.copy(
                        text = realPlayer.playerListName,
                        skin = realPlayer.name,
                        ping = getPing(player),
                    )
                }
            }
            return@map slot.copy(
                text = text,
                skin = if (slot.skin == null) null else placeholders.handlePlaceHolders(player, slot.skin),
            )
        }
        val personalLayout = layout.copy(
            slots = personalSlots,
            header = layout.header?.let { placeholders.handlePlaceHolders(player, it) },
            footer = layout.footer?.let { placeholders.handlePlaceHolders(player, it) },
        )
        if (DEBUG) {
            val diff = System.currentTimeMillis() - st
            if (diff > 30) {
                Bukkit.getLogger().info("Placeholders etc for ${player.name} took longer than expected $diff ms.")
            }
        }
        updater.updateTab(
            player,
            personalLayout,
            bypassTimeLimit
        )
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        updater.onJoin(event.player)
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, {
            updateTablist(event.player)
        }, 4)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        updater.onLeave(event.player)
    }

    // Update the tablist when the player teleports to a different world or more than 50 blocks.
    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        if (event.from.world !== event.to.world || event.from.distanceSquared(event.to) > 50 * 50) {
            val player = event.player
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, {
                if (DEBUG) {
                    Bukkit.getLogger().info("Updating (teleport) ${player.name} tablist (type: ${getLayout(player)}")
                }
                updateTablist(player, bypassTimeLimit = true)
            }, 4)
        }
    }

}