package ga.strikepractice.striketab

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

// Used to sort players in the tablist
lateinit var TAB_PRIORITY_COMPARATOR: Comparator<Player>

fun initRanks(plugin: StrikeTab) {
    val config = plugin.config
    val rankList = config.getConfigurationSection("sort-ranks").getKeys(false).map {
        config.getString("sort-ranks.$it.permission") to config.getString("sort-ranks.$it.prefix")?.translateColors()
    }.toMap()
    Bukkit.getLogger().info("Loaded priority ranks (in order): $rankList")
    TAB_PRIORITY_COMPARATOR = Comparator { a, b ->
        for (perm in rankList.keys) {
            val aHas = a.hasPermission(perm)
            val bHas = b.hasPermission(perm)
            return@Comparator aHas.compareTo(bHas)
        }
        return@Comparator 0
    }

    Bukkit.getPluginManager().registerEvents(object : Listener {
        @EventHandler
        fun onJoin(event: PlayerJoinEvent) {
            if (DEBUG) {
                println("asdasdasd")
            }
            rankList.entries.forEach { (perm, prefix) ->
                if (event.player.hasPermission(perm)) {
                    event.player.playerListName = prefix + event.player.name
                    if (DEBUG) {
                        Bukkit.getLogger()
                            .info("Set ${event.player.name}'s name to $prefix${event.player.name} because they had $perm permission")
                    }
                    return
                }
            }
        }
    }, plugin)

}

/** Get next player sorted by their rank */
fun getNextPlayer(playerIndex: Int = 1): Player? {
    var tempIndex = playerIndex
    return Bukkit.getOnlinePlayers().sortedWith(TAB_PRIORITY_COMPARATOR).firstOrNull { --tempIndex <= 0 }
}