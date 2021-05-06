package ga.strikepractice.striketab.bstats

import ga.strikepractice.striketab.StrikeTab
import ga.strikepractice.striketab.util.VIA_REWIND_SUPPORT
import ga.strikepractice.striketab.util.isLegacyClient
import org.bstats.charts.AdvancedPie
import org.bstats.charts.CustomChart
import org.bstats.charts.SimplePie
import org.bukkit.Bukkit

class CustomCharts {

    companion object {

        @JvmStatic
        fun getCustomCharts(plugin: StrikeTab): List<CustomChart> {
            return listOf(
                SimplePie("legacy_support") {
                    when (plugin.config.getBoolean("legacy-support") && legacyCanJoin()) {
                        true -> "Enabled"
                        else -> "Disabled"
                    }
                },
                SimplePie("columns") {
                    plugin.config.getInt("tablist.columns").toString()
                },
                SimplePie("header_footer_enabled") {
                    val isHeader = plugin.config.getList("header").isNotEmpty()
                    val isFooter = plugin.config.getList("footer").isNotEmpty()
                    when {
                        isHeader && isFooter -> "Header & footer"
                        isHeader -> "Header only"
                        isFooter -> "Footer only"
                        else -> "disabled"
                    }
                },
                AdvancedPie("player_version") {
                    val legacyCount = Bukkit.getOnlinePlayers().count { isLegacyClient(it) }
                    mapOf(
                        "1.7.10" to legacyCount,
                        "1.8+" to Bukkit.getOfflinePlayers().size - legacyCount
                    )
                },
                SimplePie("players_rounded") {
                    val count = Bukkit.getOnlinePlayers().size
                    when {
                        count < 5 -> "0-5"
                        count < 10 -> "5-10"
                        else -> {
                            val below = (count / 10) * 10
                            "$below-${below + 10}"
                        }
                    }
                },
            )
        }


        private fun legacyCanJoin(): Boolean {
            return VIA_REWIND_SUPPORT ||
                    Bukkit.getServer().javaClass.getPackage().name.substring(24).contains("1_7_R")
        }

    }

}
