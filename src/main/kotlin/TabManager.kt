package ga.strikepractice.striketab

import ga.strikepractice.StrikePractice
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
    private val updater: TabUpdater = DefaultTabUpdater()
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

    fun loadLayouts() {
        val header = plugin.config.getString("header").translateColors()
        val footer = plugin.config.getString("footer").translateColors()
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
                val layout = TabLayout.parse(slots.map { it.translateColors() }, header, footer)
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

    private fun getLayout(player: Player): TabLayoutType {
        val api = StrikePractice.getAPI()
        return when {
            api.isInFight(player) -> TabLayoutType.IN_MATCH
            // Add more here
            else -> TabLayoutType.DEFAULT
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
            header = placeholders.handlePlaceHolders(player, layout.header),
            footer = placeholders.handlePlaceHolders(player, layout.footer),
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


    enum class TabLayoutType {
        DEFAULT,
        IN_MATCH,
    }

    data class TabLayout(
        val slots: List<TabSlot>,
        val header: String,
        val footer: String,
    ) {

        companion object {
            fun parse(rawLines: List<String>, header: String, footer: String) =
                TabLayout(rawLines.map { TabSlot.fromString(it) }, header, footer)
        }

    }

    data class TabSlot(
        val text: String,
        val skin: String?,
        val ping: Int = 0
    ) {

        companion object {

            fun fromString(str: String): TabSlot {
                // A stupid way to make skin & ping configurable
                val skin = str.substringAfter("skin=", "").substringBefore(" ")
                val ping = str.substringAfter("ping=").substringBefore(" ")
                val text = str.replace("skin=$skin", "").replace("ping=$ping", "")
                return TabSlot(
                    text,
                    if (skin.isBlank()) null else skin,
                    ping.toIntOrNull() ?: 5
                )
            }

        }

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