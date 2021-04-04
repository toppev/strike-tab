package ga.strikepractice.striketab

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.Comparator

class RanksManager(private val plugin: StrikeTab) : Listener {

    private val rankList: Map<String, String?>

    // Used to sort players in the tablist
    private var tabPriorityComparator: Comparator<Player>

    // Current sorted tablist. First player is the "greatest" (i.e has most permissions)
    private var orderedPlayerList = listOf<UUID>()

    init {
        val config = plugin.config
        rankList = config.getConfigurationSection("sort-ranks").getKeys(false).map {
            config.getString("sort-ranks.$it.permission") to config.getString("sort-ranks.$it.prefix")
                    ?.translateColors()
        }.toMap()
        Bukkit.getLogger().info("Loaded tab ranks (in order): $rankList")
        // Greater if has more players (we reverse order the players)

        tabPriorityComparator = Comparator { a, b ->
            for (perm in rankList.keys) {
                val res = a.hasPermission(perm).compareTo(b.hasPermission(perm))
                if (res == 0) continue
                return@Comparator res
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
                debug {
                    "Set ${event.player.name}'s tab name to $prefix${event.player.name} because they had $perm permission"
                }
                return
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        Bukkit.getScheduler().runTaskLater(plugin, { refreshOrderedPlayerList() }, 1)
    }

    private fun refreshOrderedPlayerList() {
        CompletableFuture.runAsync {
            val temp = Bukkit.getOnlinePlayers().sortedWith(tabPriorityComparator.reversed()).map { it.uniqueId }
            if (DEBUG && temp != orderedPlayerList) {
                debug { "Reordered tab: ${temp.map { Bukkit.getPlayer(it)?.name }}" }
            }
            orderedPlayerList = temp
        }
    }


    /** Get the next player (by the given index) from the playerlist/tab sorted by their ranks */
    fun getNextPlayer(playerIndex: Int): Player? {
        val tempList = orderedPlayerList
        if (playerIndex < tempList.size) {
            val uuid = tempList[playerIndex]
            return Bukkit.getPlayer(uuid)
        }
        return null
    }

}