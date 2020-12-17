package ga.strikepractice.striketab

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import kotlin.Comparator

class RanksManager(private val plugin: StrikeTab) : Listener {

    private val rankList: Map<String, String?>

    // Used to sort players in the tablist
    private lateinit var TAB_PRIORITY_COMPARATOR: Comparator<Player>

    // Current sorted tablist
    private var orderedPlayerList = listOf<UUID>()

    init {
        val config = plugin.config
        rankList = config.getConfigurationSection("sort-ranks").getKeys(false).map {
            config.getString("sort-ranks.$it.permission") to config.getString("sort-ranks.$it.prefix")
                ?.translateColors()
        }.toMap()
        Bukkit.getLogger().info("Loaded tab ranks (in order): $rankList")
        TAB_PRIORITY_COMPARATOR = Comparator { a, b ->
            for (perm in rankList.keys) {
                val aHas = a.hasPermission(perm)
                val bHas = b.hasPermission(perm)
                return@Comparator aHas.compareTo(bHas)
            }
            return@Comparator 0
        }
        Bukkit.getPluginManager().registerEvents(this, plugin)
        refreshOrderedPlayerList()
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        refreshOrderedPlayerList()
        rankList.entries.forEach { (perm, prefix) ->
            if (event.player.hasPermission(perm)) {
                event.player.playerListName = prefix + event.player.name
                if (DEBUG) {
                    Bukkit.getLogger()
                        .info("Set ${event.player.name}'s tab name to $prefix${event.player.name} because they had $perm permission")
                }
                return
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        Bukkit.getScheduler().runTaskLater(plugin, { refreshOrderedPlayerList() }, 1)
    }

    private fun refreshOrderedPlayerList() {
        val temp = Bukkit.getOnlinePlayers().sortedWith(TAB_PRIORITY_COMPARATOR).map { it.uniqueId }
        if (DEBUG && temp != orderedPlayerList) {
            Bukkit.getLogger().info("Reordered tab: ${temp.map { Bukkit.getPlayer(it).name }}")
        }
        orderedPlayerList = temp
    }


    /** Get the next player (by the given index) from the playerlist/tab sorted by their ranks */
    fun getNextPlayer(playerIndex: Int): Player? {
        val tempList = orderedPlayerList
        if (playerIndex < tempList.size) {
            val uuid = tempList[playerIndex]
            return Bukkit.getPlayer(uuid)
        }
        return null;
    }

}