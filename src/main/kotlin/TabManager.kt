package ga.strikepractice.striketab

import ga.strikepractice.StrikePractice
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

const val TAB_SIZE = 80

class TabManager(private val plugin: StrikeTab) : Listener {

    private val layouts = EnumMap<TabLayoutType, TabLayout>(TabLayoutType::class.java)

    private val updater: TabUpdater = DefaultTabUpdater()
    private val placeholders = Placeholders()

    init {
        updater.onEnable(plugin)
        Bukkit.getLogger().info("Using ${updater.javaClass.name} for tablist")
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun loadLayouts() {
        TabLayoutType.values().forEach { type ->
            try {
                Bukkit.getLogger().info("Loading tablist layout for $type")
                val slots = plugin.config.getStringList("slots.${type.toString().toLowerCase()}").toMutableList()
                if (slots.size < TAB_SIZE) {
                    Bukkit.getLogger().info("Layout is missing ${TAB_SIZE - slots.size} slots. Adding empty slots")
                    for (i in slots.size until TAB_SIZE) slots.add("")
                }
                val layout = TabLayout.parse(slots)
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
            api.isInEvent(player) -> TabLayoutType.IN_EVENT
            else -> TabLayoutType.DEFAULT
        }.also {
            if (DEBUG) {
                Bukkit.getLogger().info("LayoutType for ${player.name}: $it")
            }
        }

    }

    fun setTablist(player: Player, layoutType: TabLayoutType = getLayout(player)) {
        val layout = layouts[layoutType]!!
        val st: Long = if (DEBUG) System.currentTimeMillis() else 0
        val personalSlots = layout.slots.map { slot ->
            slot.copy(
                text = placeholders.handlePlaceHolders(player, slot.text),
                skin = if (slot.skin == null) null else placeholders.handlePlaceHolders(player, slot.skin)
            )
        }
        val personalLayout = layout.copy(slots = personalSlots)
        if (DEBUG) {
            val diff = System.currentTimeMillis() - st
            if (diff > 10) {
                Bukkit.getLogger().info("Placeholders etc for ${player.name} took longer than expected $diff ms.")
            }
        }
        updater.updateTab(
            player,
            personalLayout
        )
    }


    enum class TabLayoutType {
        DEFAULT,
        IN_MATCH,
        IN_EVENT
    }

    data class TabLayout(val slots: List<TabSlot>) {

        companion object {
            fun parse(rawLines: List<String>) = TabLayout(rawLines.map { TabSlot.fromString(it) })
        }

    }

    data class TabSlot(val text: String, val skin: String?, val ping: Int = 0) {

        companion object {

            fun fromString(str: String): TabSlot {
                // A stupid way to make skin & ping configurable
                val skin = str.substringAfter("skin=", "").substringBefore(" ", "")
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
        val st = if (DEBUG) System.currentTimeMillis() else 0
        updater.onJoin(event.player)
        if (DEBUG) {
            Bukkit.getLogger().info("Initializing tablist took ${System.currentTimeMillis() - st} ms.")
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        updater.onLeave(event.player)
    }

}